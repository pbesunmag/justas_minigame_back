package com.jousting.rpg.domain.catalog;

import com.jousting.rpg.domain.enums.ArmorAffinity;

/**
 * Catálogo estático que define los valores defensivos y peso de una armadura.
 */
public class Armor implements Equipment {

    private final String id;
    private final String name;
    private final ArmorAffinity affinity;
    private final int weight;
    private final double headDefense;
    private final double chestDefense;

    // Progresión y armería
    private final int price;
    private final int requiredLevel;
    private final String requiredBossDefeat;

    public Armor(String id, String name, ArmorAffinity affinity, int weight, 
                 double headDefense, double chestDefense) {
        this(id, name, affinity, weight, headDefense, chestDefense, 0, 1, null);
    }

    public Armor(String id, String name, ArmorAffinity affinity, int weight, 
                 double headDefense, double chestDefense,
                 int price, int requiredLevel, String requiredBossDefeat) {
        this.id = id;
        this.name = name;
        this.affinity = affinity;
        this.weight = weight;
        this.headDefense = headDefense;
        this.chestDefense = chestDefense;
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

    public ArmorAffinity getAffinity() {
        return affinity;
    }

    public int getWeight() {
        return weight;
    }

    public double getHeadDefense() {
        return headDefense;
    }

    public double getChestDefense() {
        return chestDefense;
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
