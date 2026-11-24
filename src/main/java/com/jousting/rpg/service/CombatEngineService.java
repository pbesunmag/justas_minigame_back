package com.jousting.rpg.service;

import com.jousting.rpg.domain.catalog.ConsumableItem;
import com.jousting.rpg.domain.catalog.Weapon;
import com.jousting.rpg.domain.enums.ConsumableType;
import com.jousting.rpg.domain.enums.HorseOrder;
import com.jousting.rpg.domain.enums.HorseTemperament;
import com.jousting.rpg.domain.enums.RiderAction;
import com.jousting.rpg.domain.enums.TargetZone;
import com.jousting.rpg.domain.enums.WeaponType;
import com.jousting.rpg.domain.model.BattleState;
import com.jousting.rpg.domain.model.Knight;
import com.jousting.rpg.dto.PlayerTurnChoice;
import com.jousting.rpg.dto.TurnResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Motor de combate en memoria que orquesta la resolución por turnos de las justas.
 */
@Service
public class CombatEngineService {

    private final Random random;

    public CombatEngineService() {
        this(new Random());
    }

    public CombatEngineService(Random random) {
        this.random = random;
    }

    /**
     * Resuelve un turno completo de la justa siguiendo el orden estricto de reglas.
     */
    public TurnResult resolveTurn(BattleState battleState, PlayerTurnChoice choice1, PlayerTurnChoice choice2) {
        if (battleState.isBattleOver()) {
            return new TurnResult(battleState.getTurnNumber(), 
                    List.of("El combate ya ha finalizado."), true, battleState.getWinnerName());
        }

        battleState.advanceTurn();
        List<String> logs = new ArrayList<>();
        Knight k1 = battleState.getKnight1();
        Knight k2 = battleState.getKnight2();

        k1.resetTurnState();
        k2.resetTurnState();

        logs.add(String.format("=== Pase de Justa #%d ===", battleState.getTurnNumber()));

        // 1. Comprobar rendición (SURRENDER)
        if (checkSurrender(battleState, choice1, choice2, logs)) {
            return new TurnResult(battleState.getTurnNumber(), logs, true, battleState.getWinnerName());
        }

        // 2. Acciones de preparación (SWITCH_WEAPON, USE_ITEM)
        executePreparation(k1, choice1, logs);
        executePreparation(k2, choice2, logs);

        // 3. Actualizar estado del caballo (Velocidad y Fatiga)
        updateHorseState(k1, choice1.getHorseOrder(), logs);
        updateHorseState(k2, choice2.getHorseOrder(), logs);

        // 4. Aplicar posturas defensivas (DEFEND, DODGE)
        applyDefensiveStance(k1, choice1, logs);
        applyDefensiveStance(k2, choice2, logs);

        // 5. Resolver ataques simultáneos (ATTACK y ULTIMATE)
        resolveAttack(k1, k2, choice1, logs);
        resolveAttack(k2, k1, choice2, logs);

        // 6. Comprobar condición de victoria
        checkVictoryCondition(battleState, logs);

        return new TurnResult(battleState.getTurnNumber(), logs, 
                battleState.isBattleOver(), battleState.getWinnerName());
    }

    // --- Paso 1: Rendición ---
    private boolean checkSurrender(BattleState battleState, PlayerTurnChoice c1, PlayerTurnChoice c2, List<String> logs) {
        boolean k1Surrender = c1.getRiderAction() == RiderAction.SURRENDER;
        boolean k2Surrender = c2.getRiderAction() == RiderAction.SURRENDER;

        if (k1Surrender && k2Surrender) {
            logs.add("¡Ambos caballeros han arrojado sus armas! El combate concluye en retirada mutua.");
            battleState.finishBattle("EMPATE_RETIRADA");
            return true;
        } else if (k1Surrender) {
            logs.add(String.format("¡%s baja su estandarte y se rinde! %s es el vencedor.",
                    battleState.getKnight1().getName(), battleState.getKnight2().getName()));
            battleState.finishBattle(battleState.getKnight2().getName());
            return true;
        } else if (k2Surrender) {
            logs.add(String.format("¡%s baja su estandarte y se rinde! %s es el vencedor.",
                    battleState.getKnight2().getName(), battleState.getKnight1().getName()));
            battleState.finishBattle(battleState.getKnight1().getName());
            return true;
        }
        return false;
    }

    // --- Paso 2: Preparación ---
    private void executePreparation(Knight knight, PlayerTurnChoice choice, List<String> logs) {
        if (choice.getRiderAction() == RiderAction.SWITCH_WEAPON) {
            knight.switchWeapon();
            logs.add(String.format("%s cambia rápidamente a su %s.", 
                    knight.getName(), knight.getEquippedWeapon().getName()));
        } else if (choice.getRiderAction() == RiderAction.USE_ITEM) {
            ConsumableItem item = choice.getItemToUse();
            if (item != null && knight.removeItem(item)) {
                if (item.getType() == ConsumableType.HEALTH_POTION) {
                    knight.heal(item.getEffectValue());
                    logs.add(String.format("%s consume %s y recupera %d puntos de salud.",
                            knight.getName(), item.getName(), item.getEffectValue()));
                } else if (item.getType() == ConsumableType.STAMINA_POTION) {
                    knight.restoreStamina(item.getEffectValue());
                    logs.add(String.format("%s toma un trago de %s y recupera %d de resistencia.",
                            knight.getName(), item.getName(), item.getEffectValue()));
                }
            } else {
                logs.add(String.format("%s intenta usar un objeto pero no lo encuentra en su cinto.", knight.getName()));
            }
        }
    }

    // --- Paso 3: Estado del caballo ---
    private void updateHorseState(Knight knight, HorseOrder order, List<String> logs) {
        HorseTemperament temp = knight.getHorse().getTemperament();
        int speed = knight.getCurrentSpeed();
        int fatigue = knight.getCurrentFatigue();

        switch (order) {
            case ACCELERATE -> {
                speed = Math.min(knight.getHorse().getMaxSpeed(), speed + 1);
                int baseFatigueGain = (temp == HorseTemperament.CALM) ? 1 : (temp == HorseTemperament.AGGRESSIVE ? 3 : 2);
                int fatigueGain = knight.calculateFatigueGain(baseFatigueGain);
                fatigue += fatigueGain;
                logs.add(String.format("%s espolea a su montura y acelera a velocidad %d.", knight.getName(), speed));
            }
            case DECELERATE -> {
                speed = Math.max(1, speed - 1);
                fatigue = Math.max(0, fatigue - 2);
                logs.add(String.format("%s templa las riendas y decelera a velocidad %d para dar respiro al corcel.", 
                        knight.getName(), speed));
            }
            case MAINTAIN -> {
                if (speed >= 3) {
                    fatigue += 1;
                }
                logs.add(String.format("%s mantiene el galope firme a velocidad %d.", knight.getName(), speed));
            }
            case PET -> {
                int relief = (temp == HorseTemperament.NERVOUS) ? 4 : 3;
                fatigue = Math.max(0, fatigue - relief);
                logs.add(String.format("%s acaricia el cuello de su montura, calmando sus nervios (Fatiga reducida a %d/%d).",
                        knight.getName(), fatigue, knight.getHorse().getMaxFatigue()));
            }
        }

        knight.setCurrentSpeed(speed);
        knight.setCurrentFatigue(fatigue);

        // Si la fatiga llega al máximo, el caballo se desboca y pierde la acción ofensiva
        if (knight.getCurrentFatigue() >= knight.getHorse().getMaxFatigue()) {
            knight.setHorsePanicked(true);
            logs.add(String.format("¡ALERTA! El corcel de %s colapsa por fatiga extrema y se desboca sin control. ¡Pierde su acción ofensiva!",
                    knight.getName()));
        } else {
            knight.setHorsePanicked(false);
        }
    }

    // --- Paso 4: Posturas defensivas ---
    private void applyDefensiveStance(Knight knight, PlayerTurnChoice choice, List<String> logs) {
        if (choice.getRiderAction() == RiderAction.DEFEND) {
            knight.setDefending(true);
            knight.restoreStamina(5);
            logs.add(String.format("%s alza firmemente su escudo en guardia defensiva.", knight.getName()));
        } else if (choice.getRiderAction() == RiderAction.DODGE) {
            knight.setDodging(true);
            logs.add(String.format("%s se inclina sobre la silla buscando un ángulo para esquivar la embestida.", knight.getName()));
        }
    }

    // --- Paso 5: Resolución de ataques ---
    private void resolveAttack(Knight attacker, Knight defender, PlayerTurnChoice choice, List<String> logs) {
        boolean isAttack = choice.getRiderAction() == RiderAction.ATTACK;
        boolean isUltimate = choice.getRiderAction() == RiderAction.ULTIMATE;

        if (!isAttack && !isUltimate) {
            return;
        }

        // Comprobar si el caballo se desbocó
        if (attacker.isHorsePanicked()) {
            logs.add(String.format("%s lucha desesperadamente por no caer y no logra ejecutar su ofensiva.", attacker.getName()));
            return;
        }

        // Comprobar carga de Ultimate
        if (isUltimate) {
            if (!attacker.canUseUltimate()) {
                logs.add(String.format("¡%s intenta una maniobra heroica pero no tiene suficiente ímpetu (Ultimate insuficiente)!", 
                        attacker.getName()));
                return;
            }
            attacker.resetUltimateCharge();
            logs.add(String.format("¡¡¡%s DESATA SU TÉCNICA DEFINITIVA DE JUSTA!!!", attacker.getName()));
        }

        Weapon weapon = attacker.getEquippedWeapon();
        int staminaCost = weapon.getStaminaCost();
        boolean exhausted = attacker.getCurrentStamina() < staminaCost;

        if (exhausted) {
            logs.add(String.format("%s ataca falto de aliento por falta de resistencia (daño mermado).", attacker.getName()));
            attacker.consumeStamina(attacker.getCurrentStamina());
        } else {
            attacker.consumeStamina(staminaCost);
        }

        // Comprobar esquiva del rival (delegada en el defensor)
        if (defender.isDodging()) {
            double dodgeChance = defender.calculateDodgeChance();

            if (random.nextDouble() * 100.0 < dodgeChance) {
                logs.add(String.format("¡%s esquiva con una hábil finta la acometida de %s!", 
                        defender.getName(), attacker.getName()));
                defender.addUltimateCharge(15.0);
                return;
            } else {
                logs.add(String.format("%s intenta esquivar pero no logra apartarse a tiempo.", defender.getName()));
            }
        }

        // Zona y precisión
        TargetZone zone = choice.getTargetZone() != null ? choice.getTargetZone() : TargetZone.CHEST;
        double hitChance = isUltimate ? 100.0 : switch (zone) {
            case HEAD -> 60.0;
            case CHEST -> 90.0;
            case SHIELD -> 95.0;
        };

        if (random.nextDouble() * 100.0 > hitChance) {
            logs.add(String.format("%s apunta a %s pero su golpe pasa en el aire sin impactar.", 
                    attacker.getName(), zone));
            return;
        }

        // Fórmulas de daño base y modificadores
        double damage = weapon.getBaseDamage();
        if (isUltimate) {
            damage *= 2.0;
        } else if (zone == TargetZone.HEAD) {
            damage *= 1.5;
        } else if (zone == TargetZone.SHIELD) {
            damage *= 0.8;
        }

        if (exhausted) {
            damage *= 0.5;
        }

        // Bonificador de velocidad del corcel delegado en el jinete
        double speedMultiplier = attacker.calculateSpeedBonus();
        damage *= speedMultiplier;

        // Afinidad de arma contra armadura
        boolean affinityAdvantage = weapon.getEffectiveAgainst() == defender.getArmor().getAffinity();
        if (affinityAdvantage) {
            damage *= 1.20; // 20% bono por ventaja táctica
        }

        // Destino del impacto: ¿Va al escudo o al cuerpo?
        boolean hitsShield = (zone == TargetZone.SHIELD) || (defender.isDefending() && !defender.isShieldBroken());

        if (hitsShield && !defender.isShieldBroken()) {
            double shieldDmg = damage * weapon.getShieldDamageMultiplier();
            double excess = defender.damageShield(shieldDmg);

            logs.add(String.format("%s impacta violentamente contra el escudo de %s causando %.1f puntos de daño al escudo.",
                    attacker.getName(), defender.getName(), shieldDmg));

            if (defender.isShieldBroken()) {
                logs.add(String.format("¡CRACK! ¡El escudo de %s se ha quebrado en pedazos! Ahora sufrirá +25%% de daño.", 
                        defender.getName()));
            }

            if (excess > 0) {
                double bodyDamage = defender.takeDamage(excess);
                logs.add(String.format("¡La fuerza del impacto perfora la defensa e inflige %.1f de daño directo a %s!", 
                        bodyDamage, defender.getName()));
            }
        } else {
            // Impacto a la cabeza o pecho con mitigación delegada en la armadura del defensor
            double defenseValue = defender.calculateDefenseModifier(zone);
            if (affinityAdvantage) {
                defenseValue *= 0.70; // Ignora el 30% de la armadura si hay ventaja de afinidad
            }
            if (isUltimate) {
                defenseValue *= 0.50; // La ultimate perfora el 50% de la armadura
            }

            double mitigatedDamage = damage * (100.0 / (100.0 + defenseValue));
            double appliedDamage = defender.takeDamage(mitigatedDamage);

            logs.add(String.format("%s asesta un golpe en %s a %s causando %.1f puntos de daño directo.",
                    attacker.getName(), zone, defender.getName(), appliedDamage));
        }

        // Actualizar barras de Ultimate
        attacker.addUltimateCharge(15.0);
        defender.addUltimateCharge(20.0);
    }

    // --- Paso 6: Condición de victoria ---
    private void checkVictoryCondition(BattleState battleState, List<String> logs) {
        Knight k1 = battleState.getKnight1();
        Knight k2 = battleState.getKnight2();

        boolean k1Down = !k1.isAlive();
        boolean k2Down = !k2.isAlive();

        if (k1Down && k2Down) {
            logs.add("¡Ambos caballeros caen desmadejados de sus monturas simultáneamente! ¡Doble eliminación!");
            battleState.finishBattle("EMPATE_DOBLE_KO");
        } else if (k1Down) {
            logs.add(String.format("¡%s es derribado al suelo y no puede continuar! ¡%s es el campeón de la justa!",
                    k1.getName(), k2.getName()));
            battleState.finishBattle(k2.getName());
        } else if (k2Down) {
            logs.add(String.format("¡%s es derribado al suelo y no puede continuar! ¡%s es el campeón de la justa!",
                    k2.getName(), k1.getName()));
            battleState.finishBattle(k1.getName());
        }
    }
}
