package com.jousting.rpg.dto;

import java.util.Collections;
import java.util.List;

/**
 * Resultado procesado del turno que contiene los eventos narrados y el estado final de la ronda.
 */
public class TurnResult {

    private final int turnNumber;
    private final List<String> logs;
    private final boolean battleOver;
    private final String winnerName;

    public TurnResult(int turnNumber, List<String> logs, boolean battleOver, String winnerName) {
        this.turnNumber = turnNumber;
        this.logs = logs != null ? logs : Collections.emptyList();
        this.battleOver = battleOver;
        this.winnerName = winnerName;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public List<String> getLogs() {
        return logs;
    }

    public boolean isBattleOver() {
        return battleOver;
    }

    public String getWinnerName() {
        return winnerName;
    }
}
