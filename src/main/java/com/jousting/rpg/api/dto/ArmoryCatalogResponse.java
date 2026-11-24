package com.jousting.rpg.api.dto;

import java.util.List;

/**
 * Respuesta estructurada con los objetos de la armería clasificados por categoría.
 */
public record ArmoryCatalogResponse(
        List<ArmoryItemDTO> weapons,
        List<ArmoryItemDTO> armors,
        List<ArmoryItemDTO> horses
) {
}
