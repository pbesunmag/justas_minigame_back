package com.jousting.rpg.service.combate;

import com.jousting.rpg.dto.combate.DecisionPasadaDTO;
import com.jousting.rpg.dto.combate.EstadoCaballero;
import com.jousting.rpg.dto.combate.EstadoCombate;
import com.jousting.rpg.dto.combate.EstadoResultadoCombate;
import com.jousting.rpg.dto.combate.NivelGalope;
import com.jousting.rpg.dto.combate.ZonaObjetivo;
import com.jousting.rpg.model.entity.Arma;
import com.jousting.rpg.model.entity.Armadura;
import com.jousting.rpg.model.entity.Caballero;
import com.jousting.rpg.model.entity.Caballo;
import com.jousting.rpg.model.entity.Escudo;
import com.jousting.rpg.model.entity.Usuario;
import com.jousting.rpg.model.enums.TipoCaballero;
import com.jousting.rpg.model.enums.ZonaArmadura;
import com.jousting.rpg.repository.ArmaRepository;
import com.jousting.rpg.repository.ArmaduraRepository;
import com.jousting.rpg.repository.CaballeroRepository;
import com.jousting.rpg.repository.CaballoRepository;
import com.jousting.rpg.repository.EscudoRepository;
import com.jousting.rpg.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class CombateServiceTest {

    @Autowired
    private CombateService combateService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CaballeroRepository caballeroRepository;

    @Autowired
    private ArmaRepository armaRepository;

    @Autowired
    private ArmaduraRepository armaduraRepository;

    @Autowired
    private EscudoRepository escudoRepository;

    @Autowired
    private CaballoRepository caballoRepository;

    @Test
    @DisplayName("Inicialización de EstadoCaballero, ejecución de pasadas y recompensas en victoria")
    void shouldInitializeAndResolveCombatUntilVictory() {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .username("justador_test")
                .password("hash123")
                .oro(100)
                .experiencia(0)
                .nivel(1)
                .build());

        Arma lanza = armaRepository.save(Arma.builder()
                .nombre("Lanza de Torneo")
                .danoBase(35)
                .precisionBase(75)
                .peso(8)
                .precio(100)
                .build());

        Armadura yelmo = armaduraRepository.save(Armadura.builder()
                .nombre("Yelmo Cerrado")
                .zona(ZonaArmadura.CABEZA)
                .defensa(5)
                .hpExtra(15)
                .peso(4)
                .precio(60)
                .build());

        Armadura peto = armaduraRepository.save(Armadura.builder()
                .nombre("Peto de Acero")
                .zona(ZonaArmadura.TORSO)
                .defensa(8)
                .hpExtra(25)
                .peso(10)
                .precio(120)
                .build());

        Escudo escudo = escudoRepository.save(Escudo.builder()
                .nombre("Escudo Heráldico")
                .durabilidadMax(40)
                .absorcionDano(15)
                .peso(6)
                .precio(80)
                .build());

        Caballo caballo = caballoRepository.save(Caballo.builder()
                .nombre("Corcel Real")
                .velocidadMax(80)
                .resistenciaMax(50)
                .consumoStamina(15)
                .precio(200)
                .build());

        caballeroRepository.save(Caballero.builder()
                .usuario(usuario)
                .nombre("Sir Jugador")
                .tipo(TipoCaballero.JUGADOR)
                .hpBaseCabeza(60)
                .hpBaseTorso(100)
                .fuerza(20)
                .destreza(15)
                .arma(lanza)
                .yelmo(yelmo)
                .peto(peto)
                .escudo(escudo)
                .caballo(caballo)
                .build());

        Caballero rival = caballeroRepository.save(Caballero.builder()
                .nombre("Rival de Práctica")
                .tipo(TipoCaballero.SOLDADO)
                .estrategiaIa("AGRESIVA")
                .hpBaseCabeza(20)
                .hpBaseTorso(25)
                .fuerza(5)
                .destreza(5)
                .arma(lanza)
                .caballo(caballo)
                .build());

        // 1. Iniciar combate
        EstadoCombate combate = combateService.iniciarCombate(usuario.getId(), rival.getId());
        assertNotNull(combate.getCombateId());
        assertEquals(0, combate.getPasadaActual());
        assertEquals(EstadoResultadoCombate.EN_CURSO, combate.getEstado());

        // Verificar cálculos iniciales del EstadoCaballero
        EstadoCaballero j = combate.getJugador();
        assertEquals(75, j.getHpCabezaMax()); // 60 + 15
        assertEquals(125, j.getHpTorsoMax()); // 100 + 25
        assertEquals(40, j.getDurabilidadEscudoActual());
        assertFalse(j.isEscudoRoto());
        assertEquals(50, j.getStaminaActual());
        assertEquals(28, j.getPesoTotal()); // 8 + 4 + 10 + 6

        // 2. Ejecutar pasadas hasta terminar el combate
        int maxPasadas = 20;
        while (combate.getEstado() == EstadoResultadoCombate.EN_CURSO && maxPasadas-- > 0) {
            DecisionPasadaDTO decision = DecisionPasadaDTO.builder()
                    .zonaObjetivo(ZonaObjetivo.TORSO)
                    .nivelGalope(NivelGalope.CARGA_MAXIMA)
                    .usarEscudo(true)
                    .build();
            combate = combateService.ejecutarPasada(combate.getCombateId(), decision);
        }

        assertEquals(EstadoResultadoCombate.VICTORIA_JUGADOR, combate.getEstado());
        assertTrue(combate.getPasadaActual() >= 1);
        assertFalse(combate.getLogNarrativo().isEmpty());

        // Verificar recompensa guardada en Usuario (+50 oro, +100 exp por SOLDADO)
        Usuario actualizado = usuarioRepository.findById(usuario.getId()).orElseThrow();
        assertEquals(150, actualizado.getOro());
        assertEquals(100, actualizado.getExperiencia());
    }

    @Test
    @DisplayName("Validación de las tres estrategias de IA (Agresiva, Equilibrada, Defensiva)")
    void shouldValidateAllThreeAiStrategies() {
        EstadoCaballero ia = EstadoCaballero.builder()
                .hpCabezaMax(50).hpCabezaActual(20)
                .hpTorsoMax(100).hpTorsoActual(40)
                .durabilidadEscudoMax(40).durabilidadEscudoActual(40).escudoRoto(false)
                .staminaMax(50).staminaActual(50).consumoStamina(15)
                .build();

        EstadoCaballero jugador = EstadoCaballero.builder()
                .durabilidadEscudoMax(40).durabilidadEscudoActual(40).escudoRoto(false)
                .staminaMax(50).staminaActual(50).consumoStamina(15)
                .build();

        // Agresiva: CARGA_MAXIMA + CABEZA + usarEscudo = false
        DecisionPasadaDTO resAgresiva = new EstrategiaAgresiva().decidir(ia, jugador);
        assertEquals(NivelGalope.CARGA_MAXIMA, resAgresiva.getNivelGalope());
        assertEquals(ZonaObjetivo.CABEZA, resAgresiva.getZonaObjetivo());
        assertFalse(resAgresiva.isUsarEscudo());

        // Equilibrada: GALOPE + ESCUDO (jugador con escudo intacto) + cubrirse (HP < 50%)
        DecisionPasadaDTO resEquilibrada = new EstrategiaEquilibrada().decidir(ia, jugador);
        assertEquals(NivelGalope.GALOPE, resEquilibrada.getNivelGalope());
        assertEquals(ZonaObjetivo.ESCUDO, resEquilibrada.getZonaObjetivo());
        assertTrue(resEquilibrada.isUsarEscudo());

        // Defensiva: GALOPE + ESCUDO + usarEscudo = true
        DecisionPasadaDTO resDefensiva = new EstrategiaDefensiva().decidir(ia, jugador);
        assertEquals(NivelGalope.GALOPE, resDefensiva.getNivelGalope());
        assertEquals(ZonaObjetivo.ESCUDO, resDefensiva.getZonaObjetivo());
        assertTrue(resDefensiva.isUsarEscudo());
    }
}
