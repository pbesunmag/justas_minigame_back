package com.jousting.rpg.domain.catalog;

/**
 * Interfaz común para todo objeto de equipamiento disponible en el juego y la armería.
 */
public interface Equipment {

    String getId();

    String getName();

    int getPrice();

    int getRequiredLevel();

    String getRequiredBossDefeat();
}
