package com.jousting.rpg.domain.catalog;

import com.jousting.rpg.domain.enums.HorseTemperament;

/**
 * Catálogo estático que define la raza y capacidades del caballo.
 */
public class Horse implements Equipment {

    private final String id;
    private final String name;
    private final int maxSpeed; // Rango de 1 a 5
    private final int maxFatigue;
    private final HorseTemperament temperament;

    // Progresión y armería
    private final int price;
    private final int requiredLevel;
    private final String requiredBossDefeat;

    public Horse(String id, String name, int maxSpeed, int maxFatigue, HorseTemperament temperament) {
        this(id, name, maxSpeed, maxFatigue, temperament, 0, 1, null);
    }

    public Horse(String id, String name, int maxSpeed, int maxFatigue, HorseTemperament temperament,
                 int price, int requiredLevel, String requiredBossDefeat) {
        this.id = id;
        this.name = name;
        this.maxSpeed = Math.max(1, Math.min(5, maxSpeed));
        this.maxFatigue = maxFatigue;
        this.temperament = temperament;
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

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public int getMaxFatigue() {
        return maxFatigue;
    }

    public HorseTemperament getTemperament() {
        return temperament;
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
