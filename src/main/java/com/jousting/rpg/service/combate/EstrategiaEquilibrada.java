package com.jousting.rpg.service.combate;

import com.jousting.rpg.dto.combate.DecisionPasadaDTO;
import com.jousting.rpg.dto.combate.EstadoCaballero;
import com.jousting.rpg.dto.combate.NivelGalope;
import com.jousting.rpg.dto.combate.ZonaObjetivo;
import org.springframework.stereotype.Component;

/**
 * Estrategia Equilibrada (Caballero 2):
 * Alterna GALOPE, apunta a ESCUDO si el jugador tiene escudo intacto y luego a TORSO,
 * cubriéndose si su HP baja del 50%.
 */
@Component("EQUILIBRADA")
public class EstrategiaEquilibrada implements EstrategiaIA {

    @Override
    public DecisionPasadaDTO decidir(EstadoCaballero ia, EstadoCaballero jugador) {
        NivelGalope galope = ia.getStaminaActual() >= ia.getConsumoStamina()
                ? NivelGalope.GALOPE
                : NivelGalope.TROTE;

        ZonaObjetivo objetivo = (!jugador.isEscudoRoto() && jugador.getDurabilidadEscudoActual() > 0)
                ? ZonaObjetivo.ESCUDO
                : ZonaObjetivo.TORSO;

        boolean cubrirse = !ia.isEscudoRoto() && ia.getPorcentajeHpTotal() < 0.50;

        return DecisionPasadaDTO.builder()
                .zonaObjetivo(objetivo)
                .nivelGalope(galope)
                .usarEscudo(cubrirse)
                .build();
    }
}
