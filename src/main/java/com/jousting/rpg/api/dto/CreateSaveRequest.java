package com.jousting.rpg.api.dto;

/**
 * Petición para crear una nueva partida guardada con la personalización completa del caballero y su montura.
 */
public record CreateSaveRequest(
        String knightName,
        Integer age,
        String bodyType,
        String skinTone,
        String hairStyle,
        String hairColor,
        String horseName,
        String horseColor,
        String initialArmorId,
        String initialHorseId
) {
    public CreateSaveRequest(String knightName) {
        this(knightName, null, null, null, null, null, null, null, null, null);
    }
}
