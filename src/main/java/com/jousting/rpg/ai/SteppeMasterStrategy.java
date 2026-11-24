package com.jousting.rpg.ai;

import com.jousting.rpg.domain.catalog.ConsumableItem;
import com.jousting.rpg.domain.enums.ConsumableType;
import com.jousting.rpg.domain.enums.HorseOrder;
import com.jousting.rpg.domain.enums.TargetZone;
import com.jousting.rpg.domain.model.BattleState;
import com.jousting.rpg.domain.model.Knight;

import java.util.Optional;
import java.util.Random;

/**
 * Estrategia Maestra de las Estepas (Erk, Caballero de las Estepas):
 * - Maximiza la velocidad del corcel para potenciar su pasiva de daño.
 * - Monitorea la fatiga para calmar a su caballo antes de que llegue al límite.
 * - Emplea pociones curativas si su salud cae por debajo del 30%.
 * - Añade un 20% de imprevisibilidad en la zona de impacto.
 */
public class SteppeMasterStrategy implements CombatAIStrategy {

    private final Random random;

    public SteppeMasterStrategy() {
        this(new Random());
    }

    public SteppeMasterStrategy(Random random) {
        this.random = random;
    }

    @Override
    public TurnAction decideTurn(Knight self, Knight opponent, BattleState state) {
        // 1. Gestión experta de la fatiga y velocidad de la montura
        HorseOrder horseOrder;
        if (self.getCurrentFatigue() >= self.getHorse().getMaxFatigue() - 2) {
            // Acaricia al caballo para disipar la fatiga antes del colapso
            horseOrder = HorseOrder.PET;
        } else if (self.getCurrentSpeed() < self.getHorse().getMaxSpeed()) {
            horseOrder = HorseOrder.ACCELERATE;
        } else {
            horseOrder = HorseOrder.MAINTAIN;
        }

        // 2. Uso de poción si la vida desciende al 30% o menos
        if (self.getCurrentHp() <= self.getMaxHp() * 0.30) {
            Optional<ConsumableItem> potion = self.getInventory().stream()
                    .filter(item -> item.getType() == ConsumableType.HEALTH_POTION)
                    .findFirst();

            if (potion.isPresent()) {
                return TurnAction.useItem(potion.get(), horseOrder);
            }
        }

        // 3. Determinación de zona con 20% de aleatoriedad táctica
        TargetZone chosenZone = determineTargetZone(opponent);

        // 4. Técnica definitiva si la barra está completa
        if (self.canUseUltimate()) {
            return TurnAction.ultimate(chosenZone, horseOrder);
        }

        // 5. Ataque montado
        return TurnAction.attack(chosenZone, horseOrder);
    }

    private TargetZone determineTargetZone(Knight opponent) {
        // 20% de probabilidad de finta imprevisible a cualquier zona
        if (random.nextDouble() < 0.20) {
            TargetZone[] zones = TargetZone.values();
            return zones[random.nextInt(zones.length)];
        }

        // 80% lógica óptima: castiga el pecho o la cabeza si el escudo ya cayó
        if (opponent.isShieldBroken()) {
            return TargetZone.HEAD;
        }
        return TargetZone.CHEST;
    }
}
