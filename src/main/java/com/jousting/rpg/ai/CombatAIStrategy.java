package com.jousting.rpg.ai;

import com.jousting.rpg.domain.model.BattleState;
import com.jousting.rpg.domain.model.Knight;

/**
 * Interfaz del patrón Strategy para la toma de decisiones tácticas de la IA en combate PvE.
 */
public interface CombatAIStrategy {

    /**
     * Decide la acción de jinete y la orden de montura en función del estado de la batalla.
     */
    TurnAction decideTurn(Knight self, Knight opponent, BattleState state);
}
