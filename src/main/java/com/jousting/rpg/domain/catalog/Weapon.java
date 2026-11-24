package com.jousting.rpg.domain.catalog;

import com.jousting.rpg.domain.enums.ArmorAffinity;
import com.jousting.rpg.domain.enums.WeaponType;

/**
 * Catálogo estático que define las características de un arma.
 */
public class Weapon implements Equipment {

    private final String id;
    private final String name;
    private final WeaponType type;
    private final double baseDamage;
    private final double shieldDamageMultiplier;
    private final int staminaCost;
    private final ArmorAffinity effectiveAgainst;

    // Progresión y armería
    private final int price;
    private final int requiredLevel;
    private final String requiredBossDefeat;

    public Weapon(String id, String name, WeaponType type, double baseDamage, 
                  double shieldDamageMultiplier, int staminaCost, ArmorAffinity effectiveAgainst) {
        this(id, name, type, baseDamage, shieldDamageMultiplier, staminaCost, effectiveAgainst, 0, 1, null);
    }

    public Weapon(String id, String name, WeaponType type, double baseDamage, 
                  double shieldDamageMultiplier, int staminaCost, ArmorAffinity effectiveAgainst,
                  int price, int requiredLevel, String requiredBossDefeat) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.baseDamage = baseDamage;
        this.shieldDamageMultiplier = shieldDamageMultiplier;
        this.staminaCost = staminaCost;
        this.effectiveAgainst = effectiveAgainst;
        this.price = price;
        this.requiredLevel = requiredLevel;
        this.requiredBossDefeat = requiredBossDefeat;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public WeaponType getType() {
        return type;
    }

    public double getBaseDamage() {
        return baseDamage;
    }

    public double getShieldDamageMultiplier() {
        return shieldDamageMultiplier;
    }

    public int getStaminaCost() {
        return staminaCost;
    }

    public ArmorAffinity getEffectiveAgainst() {
        return effectiveAgainst;
    }

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public int getRequiredLevel() {
        return requiredLevel;
    }

    @Override
    public String getRequiredBossDefeat() {
        return requiredBossDefeat;
    }
}
