package com.jousting.rpg.api.dto;

import com.jousting.rpg.dto.RewardSummary;

import java.util.List;

/**
 * Respuesta completa de un turno de combate incluyendo narrativa, consejo táctico de Cassian y recompensas.
 */
public record BattleResponseDTO(
        String battleId,
        int turnNumber,
        String status,
        KnightBattleStatusDTO player,
        KnightBattleStatusDTO enemy,
        List<String> turnNarrative,
        RewardSummary reward,
        String cassianTip
) {
}
