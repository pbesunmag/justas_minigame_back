package com.jousting.rpg.dto.combate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionPasadaDTO {
    private ZonaObjetivo zonaObjetivo;
    private NivelGalope nivelGalope;
    private boolean usarEscudo;
}
