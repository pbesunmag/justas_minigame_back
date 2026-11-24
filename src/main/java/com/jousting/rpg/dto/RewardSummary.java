package com.jousting.rpg.dto;

import java.util.Collections;
import java.util.List;

/**
 * Resumen de recompensas, experiencia obtenida y objetos desbloqueados tras una victoria.
 */
public class RewardSummary {

    private final int goldEarned;
    private final int xpEarned;
    private final boolean firstVictory;
    private final boolean leveledUp;
    private final int newLevel;
    private final List<String> newlyAvailableItems;
    private final List<String> logs;

    public RewardSummary(int goldEarned, int xpEarned, boolean firstVictory, boolean leveledUp, 
                         int newLevel, List<String> newlyAvailableItems, List<String> logs) {
        this.goldEarned = goldEarned;
        this.xpEarned = xpEarned;
        this.firstVictory = firstVictory;
        this.leveledUp = leveledUp;
        this.newLevel = newLevel;
        this.newlyAvailableItems = newlyAvailableItems != null ? newlyAvailableItems : Collections.emptyList();
        this.logs = logs != null ? logs : Collections.emptyList();
    }

    public int getGoldEarned() {
        return goldEarned;
    }

    public int getXpEarned() {
        return xpEarned;
    }

    public boolean isFirstVictory() {
        return firstVictory;
    }

    public boolean isLeveledUp() {
        return leveledUp;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public List<String> getNewlyAvailableItems() {
        return newlyAvailableItems;
    }

    public List<String> getLogs() {
        return logs;
    }
}
