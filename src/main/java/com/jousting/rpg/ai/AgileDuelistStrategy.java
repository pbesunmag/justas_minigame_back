package com.jousting.rpg.ai;

import com.jousting.rpg.domain.enums.HorseOrder;
import com.jousting.rpg.domain.enums.TargetZone;
import com.jousting.rpg.domain.model.BattleState;
import com.jousting.rpg.domain.model.Knight;

/**
 * Estrategia de Duelista Ágil (Lady Valerie la Centella):
 * - Busca galopar a gran velocidad.
 * - Esquiva ágilmente cuando el rival carga a velocidad alta (>= 3).
 * - Busca golpes letales y críticos a la cabeza del oponente.
 */
public class AgileDuelistStrategy implements CombatAIStrategy {

    @Override
    public TurnAction decideTurn(Knight self, Knight opponent, BattleState state) {
        // 1. Gestión de la velocidad del corcel
        HorseOrder horseOrder;
        if (self.getCurrentFatigue() >= self.getHorse().getMaxFatigue() - 2) {
            horseOrder = HorseOrder.DECELERATE;
        } else if (self.getCurrentSpeed() < self.getHorse().getMaxSpeed()) {
            horseOrder = HorseOrder.ACCELERATE;
        } else {
            horseOrder = HorseOrder.MAINTAIN;
        }

        // 2. Técnica definitiva a la cabeza
        if (self.canUseUltimate()) {
            return TurnAction.ultimate(TargetZone.HEAD, horseOrder);
        }

        // 3. Maniobra evasiva si el adversario viene lanzado a velocidad peligrosa
        if (opponent.getCurrentSpeed() >= 3 && self.getCurrentStamina() >= 10) {
            return TurnAction.dodge(horseOrder);
        }

        // 4. Ataque preciso y crítico a la cabeza
        return TurnAction.attack(TargetZone.HEAD, horseOrder);
    }
}
