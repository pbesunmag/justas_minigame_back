package com.jousting.rpg.service.combate;

import com.jousting.rpg.dto.combate.DecisionPasadaDTO;
import com.jousting.rpg.dto.combate.EstadoCaballero;
import com.jousting.rpg.dto.combate.NivelGalope;
import com.jousting.rpg.dto.combate.ZonaObjetivo;
import org.springframework.stereotype.Component;

/**
 * Estrategia Defensiva (Erk, el hostigador):
 * Gestiona stamina con TROTE/GALOPE, mantiene usarEscudo = true mientras tenga escudo,
 * desgasta el escudo rival primero y remata al TORSO o CABEZA cuando el jugador se queda sin stamina o sin escudo.
 */
@Component("DEFENSIVA")
public class EstrategiaDefensiva implements EstrategiaIA {

    @Override
    public DecisionPasadaDTO decidir(EstadoCaballero ia, EstadoCaballero jugador) {
        boolean staminaBaja = ia.getStaminaMax() > 0 && ia.getStaminaActual() < (ia.getStaminaMax() * 0.40);
        NivelGalope galope = (!staminaBaja && ia.getStaminaActual() >= ia.getConsumoStamina())
                ? NivelGalope.GALOPE
                : NivelGalope.TROTE;

        boolean usarEscudo = !ia.isEscudoRoto() && ia.getDurabilidadEscudoActual() > 0;

        ZonaObjetivo objetivo;
        boolean jugadorSinStamina = jugador.getStaminaActual() < jugador.getConsumoStamina();
        if (!jugador.isEscudoRoto() && jugador.getDurabilidadEscudoActual() > 0) {
            objetivo = ZonaObjetivo.ESCUDO;
        } else if (jugadorSinStamina) {
            objetivo = ZonaObjetivo.CABEZA;
        } else {
            objetivo = ZonaObjetivo.TORSO;
        }

        return DecisionPasadaDTO.builder()
                .zonaObjetivo(objetivo)
                .nivelGalope(galope)
                .usarEscudo(usarEscudo)
                .build();
    }
}
