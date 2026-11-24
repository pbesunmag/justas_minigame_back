package com.jousting.rpg.service.combate;

import com.jousting.rpg.dto.combate.DecisionPasadaDTO;
import com.jousting.rpg.dto.combate.EstadoCaballero;
import com.jousting.rpg.dto.combate.NivelGalope;
import com.jousting.rpg.dto.combate.ZonaObjetivo;
import org.springframework.stereotype.Component;

/**
 * Estrategia Agresiva (Caballero 1):
 * Prioriza CARGA_MAXIMA si tiene stamina y apunta a CABEZA sin cubrirse (usarEscudo = false)
 * para buscar el KO rápido.
 */
@Component("AGRESIVA")
public class EstrategiaAgresiva implements EstrategiaIA {

    @Override
    public DecisionPasadaDTO decidir(EstadoCaballero ia, EstadoCaballero jugador) {
        int costeCarga = (int) Math.ceil(ia.getConsumoStamina() * 1.5);
        int costeGalope = ia.getConsumoStamina();

        NivelGalope galope;
        if (ia.getStaminaActual() >= costeCarga) {
            galope = NivelGalope.CARGA_MAXIMA;
        } else if (ia.getStaminaActual() >= costeGalope) {
            galope = NivelGalope.GALOPE;
        } else {
            galope = NivelGalope.TROTE;
        }

        return DecisionPasadaDTO.builder()
                .zonaObjetivo(ZonaObjetivo.CABEZA)
                .nivelGalope(galope)
                .usarEscudo(false)
                .build();
    }
}
