package com.jousting.rpg.api.dto;

/**
 * Petición para iniciar un nuevo combate contra un enemigo específico.
 */
public record StartBattleRequest(String enemyCode) {
}
