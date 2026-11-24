package com.jousting.rpg.domain.model;

import com.jousting.rpg.domain.catalog.Armor;
import com.jousting.rpg.domain.catalog.ConsumableItem;
import com.jousting.rpg.domain.catalog.Horse;
import com.jousting.rpg.domain.catalog.Weapon;
import com.jousting.rpg.domain.enums.TargetZone;

import java.util.List;

/**
 * Subclase especializada para "Erk, Caballero de las Estepas".
 * Posee la pasiva "Viento de las Estepas":
 * 1. Mayor daño por velocidad con cualquier arma (+0.35 por velocidad en vez de +0.15).
 * 2. Menor fatiga generada en el caballo gracias a su técnica nómada de monta.
 */
public class ErkSteppeKnight extends Knight {

    public ErkSteppeKnight(String name, double maxHp, int maxStamina, double maxShieldHp,
                           Weapon mainWeapon, Weapon secondaryWeapon, Armor armor, Horse horse,
                           List<ConsumableItem> initialItems) {
        super(name, maxHp, maxStamina, maxShieldHp, mainWeapon, secondaryWeapon, armor, horse, initialItems);
    }

    public ErkSteppeKnight(String name, double maxHp, int maxStamina, double maxShieldHp,
                           Weapon mainWeapon, Weapon secondaryWeapon, Armor armor, Horse horse,
                           List<ConsumableItem> initialItems, com.jousting.rpg.ai.CombatAIStrategy aiStrategy) {
        this(name, maxHp, maxStamina, maxShieldHp, mainWeapon, secondaryWeapon, armor, horse, initialItems, aiStrategy, null);
    }

    public ErkSteppeKnight(String name, double maxHp, int maxStamina, double maxShieldHp,
                           Weapon mainWeapon, Weapon secondaryWeapon, Armor armor, Horse horse,
                           List<ConsumableItem> initialItems, com.jousting.rpg.ai.CombatAIStrategy aiStrategy,
                           BattleReward battleReward) {
        super(name, maxHp, maxStamina, maxShieldHp, mainWeapon, secondaryWeapon, armor, horse, initialItems, aiStrategy, battleReward);
    }

    @Override
    public double calculateSpeedBonus() {
        // Aprovecha la inercia del galope abierto con cualquier arma equipada
        return 1.0 + (getCurrentSpeed() * 0.35);
    }

    @Override
    public int calculateFatigueGain(int baseGain) {
        // Su montura de las estepas sufre menos desgaste al acelerar
        return Math.max(1, baseGain - 1);
    }

    @Override
    public String getPassiveDescription() {
        return "Viento de las Estepas (Escalado de velocidad +35% en cualquier arma y -1 fatiga al acelerar)";
    }
}
