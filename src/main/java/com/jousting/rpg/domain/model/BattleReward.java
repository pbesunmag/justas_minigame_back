package com.jousting.rpg.domain.model;

/**
 * Recompensa otorgada al vencer a un rival en combate.
 */
public class BattleReward {

    private final String enemyCode;
    private final int gold;
    private final int experience;

    public BattleReward(String enemyCode, int gold, int experience) {
        this.enemyCode = enemyCode;
        this.gold = gold;
        this.experience = experience;
    }

    public String getEnemyCode() {
        return enemyCode;
    }

    public int getGold() {
        return gold;
    }

    public int getExperience() {
        return experience;
    }
}
