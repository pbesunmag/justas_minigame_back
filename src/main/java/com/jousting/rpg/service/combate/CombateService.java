package com.jousting.rpg.service.combate;

import com.jousting.rpg.dto.combate.DecisionPasadaDTO;
import com.jousting.rpg.dto.combate.EstadoCaballero;
import com.jousting.rpg.dto.combate.EstadoCombate;
import com.jousting.rpg.dto.combate.EstadoResultadoCombate;
import com.jousting.rpg.dto.combate.NivelGalope;
import com.jousting.rpg.dto.combate.ZonaObjetivo;
import com.jousting.rpg.model.entity.Caballero;
import com.jousting.rpg.model.entity.Usuario;
import com.jousting.rpg.model.enums.TipoCaballero;
import com.jousting.rpg.repository.CaballeroRepository;
import com.jousting.rpg.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Motor de combate táctico por turnos (pasadas de justa) en memoria.
 */
@Service
public class CombateService {

    private static final int ORO_SOLDADO = 50;
    private static final int EXP_SOLDADO = 100;
    private static final int ORO_JEFE = 150;
    private static final int EXP_JEFE = 300;

    private final Map<String, EstadoCombate> combatesActivos = new ConcurrentHashMap<>();
    private final CaballeroRepository caballeroRepository;
    private final UsuarioRepository usuarioRepository;
    private final Map<String, EstrategiaIA> estrategias;
    private final Random random;

    @Autowired
    public CombateService(CaballeroRepository caballeroRepository,
                          UsuarioRepository usuarioRepository,
                          Map<String, EstrategiaIA> estrategias) {
        this(caballeroRepository, usuarioRepository, estrategias, new Random());
    }

    CombateService(CaballeroRepository caballeroRepository,
                   UsuarioRepository usuarioRepository,
                   Map<String, EstrategiaIA> estrategias,
                   Random random) {
        this.caballeroRepository = caballeroRepository;
        this.usuarioRepository = usuarioRepository;
        this.estrategias = estrategias;
        this.random = random;
    }

    /**
     * Inicia un nuevo combate en memoria cargando los datos iniciales del jugador y rival desde MySQL.
     */
    public EstadoCombate iniciarCombate(Long usuarioId, Long rivalId) {
        Caballero caballeroJugador = caballeroRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new NoSuchElementException("No se encontró un caballero asociado al usuario con ID: " + usuarioId));

        Caballero caballeroRival = caballeroRepository.findById(rivalId)
                .orElseThrow(() -> new NoSuchElementException("No se encontró el caballero rival con ID: " + rivalId));

        EstadoCaballero estadoJugador = EstadoCaballero.desdeEntidad(caballeroJugador);
        EstadoCaballero estadoRival = EstadoCaballero.desdeEntidad(caballeroRival);

        String combateId = UUID.randomUUID().toString();
        List<String> logInicial = new ArrayList<>();
        logInicial.add(String.format("¡Comienza la justa! %s se enfrenta a %s.",
                estadoJugador.getNombre(), estadoRival.getNombre()));

        EstadoCombate estadoCombate = EstadoCombate.builder()
                .combateId(combateId)
                .usuarioId(usuarioId)
                .pasadaActual(0)
                .estado(EstadoResultadoCombate.EN_CURSO)
                .logNarrativo(logInicial)
                .jugador(estadoJugador)
                .rival(estadoRival)
                .build();

        combatesActivos.put(combateId, estadoCombate);
        return estadoCombate;
    }

    /**
     * Ejecuta una pasada simultánea de justa entre el jugador y la IA rival.
     */
    public EstadoCombate ejecutarPasada(String combateId, DecisionPasadaDTO decisionJugador) {
        EstadoCombate combate = combatesActivos.get(combateId);
        if (combate == null) {
            throw new NoSuchElementException("No existe ningún combate activo con ID: " + combateId);
        }

        if (combate.getEstado() != EstadoResultadoCombate.EN_CURSO) {
            throw new IllegalStateException("El combate ya ha finalizado con estado: " + combate.getEstado());
        }

        DecisionPasadaDTO jugadaJugador = normalizarDecision(decisionJugador);
        EstadoCaballero jugador = combate.getJugador();
        EstadoCaballero rival = combate.getRival();

        EstrategiaIA estrategiaRival = resolverEstrategia(rival.getEstrategiaIa());
        DecisionPasadaDTO jugadaRival = normalizarDecision(estrategiaRival.decidir(rival, jugador));

        combate.setPasadaActual(combate.getPasadaActual() + 1);
        List<String> log = new ArrayList<>();
        log.add(String.format("=== Pasada #%d ===", combate.getPasadaActual()));

        // 1. Resolver galope y stamina de ambos combatientes
        NivelGalope galopeJugador = aplicarGalopeYStamina(jugador, jugadaJugador.getNivelGalope(), log);
        NivelGalope galopeRival = aplicarGalopeYStamina(rival, jugadaRival.getNivelGalope(), log);

        double velocidadJugador = calcularVelocidadEfectiva(jugador, galopeJugador);
        double velocidadRival = calcularVelocidadEfectiva(rival, galopeRival);

        // 2. Resolver impactos de la pasada
        resolverAtaque(jugador, jugadaJugador, velocidadJugador, rival, jugadaRival, log);
        resolverAtaque(rival, jugadaRival, velocidadRival, jugador, jugadaJugador, log);

        // 3. Evaluar condición de victoria y persistir recompensas si gana el jugador
        if (rival.estaDerrotado()) {
            combate.setEstado(EstadoResultadoCombate.VICTORIA_JUGADOR);
            String motivo = rival.getHpCabezaActual() <= 0
                    ? "un golpe crítico a la cabeza que lo descabalga"
                    : "daño letal al torso";
            log.add(String.format("¡VICTORIA! %s derrota a %s mediante %s.",
                    jugador.getNombre(), rival.getNombre(), motivo));
            otorgarRecompensaVictoria(combate.getUsuarioId(), rival, log);
        } else if (jugador.estaDerrotado()) {
            combate.setEstado(EstadoResultadoCombate.DERROTA_JUGADOR);
            String motivo = jugador.getHpCabezaActual() <= 0
                    ? "un golpe crítico a la cabeza"
                    : "daño letal al torso";
            log.add(String.format("DERROTA: %s cae de su montura tras recibir %s.",
                    jugador.getNombre(), motivo));
        }

        combate.setLogNarrativo(log);
        return combate;
    }

    public EstadoCombate obtenerCombate(String combateId) {
        EstadoCombate combate = combatesActivos.get(combateId);
        if (combate == null) {
            throw new NoSuchElementException("No existe ningún combate activo con ID: " + combateId);
        }
        return combate;
    }

    private DecisionPasadaDTO normalizarDecision(DecisionPasadaDTO decision) {
        if (decision == null) {
            return DecisionPasadaDTO.builder()
                    .zonaObjetivo(ZonaObjetivo.TORSO)
                    .nivelGalope(NivelGalope.GALOPE)
                    .usarEscudo(true)
                    .build();
        }
        ZonaObjetivo zona = decision.getZonaObjetivo() != null ? decision.getZonaObjetivo() : ZonaObjetivo.TORSO;
        NivelGalope galope = decision.getNivelGalope() != null ? decision.getNivelGalope() : NivelGalope.GALOPE;
        return DecisionPasadaDTO.builder()
                .zonaObjetivo(zona)
                .nivelGalope(galope)
                .usarEscudo(decision.isUsarEscudo())
                .build();
    }

    private EstrategiaIA resolverEstrategia(String nombreEstrategia) {
        if (nombreEstrategia == null || nombreEstrategia.isBlank()) {
            return estrategias.getOrDefault("EQUILIBRADA", new EstrategiaEquilibrada());
        }
        String clave = nombreEstrategia.trim().toUpperCase();
        if (clave.contains("AGRESIVA")) {
            return estrategias.getOrDefault("AGRESIVA", new EstrategiaAgresiva());
        }
        if (clave.contains("DEFENSIVA") || clave.contains("ERK")) {
            return estrategias.getOrDefault("DEFENSIVA", new EstrategiaDefensiva());
        }
        return estrategias.getOrDefault("EQUILIBRADA", new EstrategiaEquilibrada());
    }

    private NivelGalope aplicarGalopeYStamina(EstadoCaballero caballero, NivelGalope galopeElegido, List<String> log) {
        int coste = calcularCosteStamina(caballero, galopeElegido);
        NivelGalope galopeEfectivo = galopeElegido;

        if (galopeElegido != NivelGalope.TROTE && caballero.getStaminaActual() < coste) {
            galopeEfectivo = NivelGalope.TROTE;
            log.add(String.format("%s no tiene stamina suficiente para %s y baja automáticamente a TROTE.",
                    caballero.getNombre(), galopeElegido));
        }

        if (galopeEfectivo == NivelGalope.TROTE) {
            int nuevaStamina = Math.min(caballero.getStaminaMax(), caballero.getStaminaActual() + 10);
            caballero.setStaminaActual(nuevaStamina);
            log.add(String.format("%s avanza al TROTE y recupera stamina (%d/%d).",
                    caballero.getNombre(), caballero.getStaminaActual(), caballero.getStaminaMax()));
        } else {
            caballero.setStaminaActual(Math.max(0, caballero.getStaminaActual() - coste));
            log.add(String.format("%s carga a %s (Stamina: %d/%d).",
                    caballero.getNombre(), galopeEfectivo, caballero.getStaminaActual(), caballero.getStaminaMax()));
        }

        return galopeEfectivo;
    }

    private int calcularCosteStamina(EstadoCaballero caballero, NivelGalope galope) {
        return switch (galope) {
            case TROTE -> 0;
            case GALOPE -> caballero.getConsumoStamina();
            case CARGA_MAXIMA -> (int) Math.ceil(caballero.getConsumoStamina() * 1.5);
        };
    }

    double calcularVelocidadEfectiva(EstadoCaballero caballero, NivelGalope galope) {
        double velocidadBase = Math.max(1.0, caballero.getVelocidadMax() - (caballero.getPesoTotal() * 0.8));
        return Math.max(1.0, velocidadBase * galope.getMultiplicadorVelocidad());
    }

    double calcularProbabilidadAcierto(EstadoCaballero atacante, DecisionPasadaDTO decision) {
        double prob = atacante.getPrecisionBaseArma()
                + (atacante.getDestreza() * 1.5)
                + (decision.isUsarEscudo() ? 0 : 15)
                - (decision.getZonaObjetivo() == ZonaObjetivo.CABEZA ? 20 : 0);
        return Math.max(15.0, Math.min(95.0, prob));
    }

    double calcularDanoBruto(EstadoCaballero atacante, double velocidadEfectiva) {
        return (atacante.getDanoBaseArma() + (atacante.getFuerza() * 0.6)) * (velocidadEfectiva / 70.0);
    }

    private void resolverAtaque(EstadoCaballero atacante,
                                DecisionPasadaDTO jugadaAtacante,
                                double velocidadEfectiva,
                                EstadoCaballero defensor,
                                DecisionPasadaDTO jugadaDefensor,
                                List<String> log) {

        double probAcierto = calcularProbabilidadAcierto(atacante, jugadaAtacante);
        double tirada = random.nextDouble() * 100.0;

        if (tirada > probAcierto) {
            log.add(String.format("%s apunta a %s (%.0f%%) pero falla el golpe.",
                    atacante.getNombre(), jugadaAtacante.getZonaObjetivo(), probAcierto));
            return;
        }

        double danoBruto = calcularDanoBruto(atacante, velocidadEfectiva);
        ZonaObjetivo objetivo = jugadaAtacante.getZonaObjetivo();
        boolean defensorCubiertoConEscudo = jugadaDefensor.isUsarEscudo() && !defensor.isEscudoRoto();

        if (objetivo == ZonaObjetivo.ESCUDO) {
            if (!defensor.isEscudoRoto()) {
                int danoEscudo = Math.max(1, (int) Math.round(danoBruto));
                aplicarDanoAEscudo(atacante, defensor, danoEscudo, log);
            } else {
                // Si el escudo ya está roto, el impacto pasa directamente al torso
                aplicarDanoDirectoAZona(atacante, defensor, ZonaObjetivo.TORSO, danoBruto, log);
            }
            return;
        }

        // Objetivo es CABEZA o TORSO
        if (defensorCubiertoConEscudo && random.nextBoolean()) {
            // 50% de probabilidad de intercepción con escudo
            int danoMitigado = Math.max(1, (int) Math.round(Math.min(danoBruto, defensor.getAbsorcionDanoEscudo())));
            double danoRestante = Math.max(0.0, danoBruto - danoMitigado);

            log.add(String.format("¡El escudo de %s intercepta el golpe dirigido a %s!",
                    defensor.getNombre(), objetivo));
            aplicarDanoAEscudo(atacante, defensor, danoMitigado, log);

            if (danoRestante > 0) {
                aplicarDanoDirectoAZona(atacante, defensor, objetivo, danoRestante, log);
            }
        } else {
            aplicarDanoDirectoAZona(atacante, defensor, objetivo, danoBruto, log);
        }
    }

    private void aplicarDanoAEscudo(EstadoCaballero atacante, EstadoCaballero defensor, int danoEscudo, List<String> log) {
        int nuevaDurabilidad = Math.max(0, defensor.getDurabilidadEscudoActual() - danoEscudo);
        defensor.setDurabilidadEscudoActual(nuevaDurabilidad);
        log.add(String.format("%s golpea el escudo de %s causando %d de desgaste (Durabilidad: %d/%d).",
                atacante.getNombre(), defensor.getNombre(), danoEscudo,
                defensor.getDurabilidadEscudoActual(), defensor.getDurabilidadEscudoMax()));

        if (defensor.getDurabilidadEscudoActual() <= 0) {
            defensor.setEscudoRoto(true);
            log.add(String.format("¡CRACK! El escudo de %s se ha hecho añicos y ya no podrá bloquear.",
                    defensor.getNombre()));
        }
    }

    private void aplicarDanoDirectoAZona(EstadoCaballero atacante,
                                         EstadoCaballero defensor,
                                         ZonaObjetivo zona,
                                         double danoEntrante,
                                         List<String> log) {
        int defensa = (zona == ZonaObjetivo.CABEZA) ? defensor.getDefensaCabeza() : defensor.getDefensaTorso();
        int danoFinal = Math.max(1, (int) Math.round(danoEntrante - defensa));

        if (zona == ZonaObjetivo.CABEZA) {
            defensor.setHpCabezaActual(Math.max(0, defensor.getHpCabezaActual() - danoFinal));
            log.add(String.format("%s impacta en la CABEZA de %s por %d de daño (HP Cabeza: %d/%d).",
                    atacante.getNombre(), defensor.getNombre(), danoFinal,
                    defensor.getHpCabezaActual(), defensor.getHpCabezaMax()));
        } else {
            defensor.setHpTorsoActual(Math.max(0, defensor.getHpTorsoActual() - danoFinal));
            log.add(String.format("%s impacta en el TORSO de %s por %d de daño (HP Torso: %d/%d).",
                    atacante.getNombre(), defensor.getNombre(), danoFinal,
                    defensor.getHpTorsoActual(), defensor.getHpTorsoMax()));
        }
    }

    private void otorgarRecompensaVictoria(Long usuarioId, EstadoCaballero rival, List<String> log) {
        if (usuarioId == null) {
            return;
        }
        usuarioRepository.findById(usuarioId).ifPresent(usuario -> {
            boolean esJefe = rival.getTipo() == TipoCaballero.JEFE;
            int oroGanado = esJefe ? ORO_JEFE : ORO_SOLDADO;
            int expGanada = esJefe ? EXP_JEFE : EXP_SOLDADO;

            int oroActual = usuario.getOro() != null ? usuario.getOro() : 0;
            int expActual = usuario.getExperiencia() != null ? usuario.getExperiencia() : 0;

            usuario.setOro(oroActual + oroGanado);
            usuario.setExperiencia(expActual + expGanada);
            usuarioRepository.save(usuario);

            log.add(String.format("Recompensa obtenida: +%d oro y +%d experiencia (Total: %d oro, %d exp).",
                    oroGanado, expGanada, usuario.getOro(), usuario.getExperiencia()));
        });
    }
}
