package com.jousting.rpg.ai;

import com.jousting.rpg.domain.enums.HorseOrder;
import com.jousting.rpg.domain.enums.TargetZone;
import com.jousting.rpg.domain.model.BattleState;
import com.jousting.rpg.domain.model.Knight;

/**
 * Estrategia de Tanque Defensivo (Sir Gideon el Muralla):
 * - Prioriza quebrar el escudo rival.
 * - Pasa a postura defensiva si su resistencia decae para reponer aliento.
 * - Gestiona cautelosamente la fatiga del corcel percherón.
 */
public class DefensiveTankStrategy implements CombatAIStrategy {

    @Override
    public TurnAction decideTurn(Knight self, Knight opponent, BattleState state) {
        // 1. Gestión de orden al corcel
        HorseOrder horseOrder;
        double fatigueRatio = (double) self.getCurrentFatigue() / self.getHorse().getMaxFatigue();

        if (fatigueRatio >= 0.60) {
            horseOrder = HorseOrder.PET;
        } else if (self.getCurrentSpeed() < 2) {
            horseOrder = HorseOrder.ACCELERATE;
        } else {
            horseOrder = HorseOrder.MAINTAIN;
        }

        // 2. Comprobar técnica definitiva
        if (self.canUseUltimate()) {
            TargetZone ultZone = opponent.isShieldBroken() ? TargetZone.HEAD : TargetZone.CHEST;
            return TurnAction.ultimate(ultZone, horseOrder);
        }

        // 3. Evaluar nivel de stamina para entrar en guardia
        int staminaCost = self.getEquippedWeapon().getStaminaCost();
        if (self.getCurrentStamina() < staminaCost || self.getCurrentStamina() <= 15) {
            return TurnAction.defend(horseOrder);
        }

        // 4. Ofensiva: priorizar destruir el escudo antes de atacar al cuerpo
        TargetZone targetZone = !opponent.isShieldBroken() ? TargetZone.SHIELD : TargetZone.CHEST;
        return TurnAction.attack(targetZone, horseOrder);
    }
}
