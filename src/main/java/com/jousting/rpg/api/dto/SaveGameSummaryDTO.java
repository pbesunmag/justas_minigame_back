package com.jousting.rpg.api.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * DTO de salida con el resumen del progreso y personalización de la partida guardada.
 */
public record SaveGameSummaryDTO(
        Long id,
        String knightName,
        int age,
        String bodyType,
        String skinTone,
        String hairStyle,
        String hairColor,
        String horseName,
        String horseColor,
        int level,
        int experience,
        int gold,
        int maxHp,
        int maxStamina,
        String mainWeaponId,
        String secondaryWeaponId,
        String armorId,
        String horseId,
        Set<String> defeatedBosses,
        Map<String, Integer> consumables,
        LocalDateTime lastSavedAt
) {
}
