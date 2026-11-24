package com.jousting.rpg.api.dto;

import com.jousting.rpg.domain.enums.ItemStatus;

import java.util.Map;

/**
 * DTO que representa un objeto de la armería categorizado con estadísticas y estado.
 */
public record ArmoryItemDTO(
        String id,
        String name,
        Map<String, Object> stats,
        int price,
        int requiredLevel,
        String requiredBossDefeat,
        ItemStatus status,
        String lockReason
) {
    public ArmoryItemDTO(String id, String name, String category, Map<String, Object> stats,
                         int price, int requiredLevel, String requiredBossDefeat,
                         ItemStatus status, String lockReason) {
        this(id, name, stats, price, requiredLevel, requiredBossDefeat, status, lockReason);
    }
}
