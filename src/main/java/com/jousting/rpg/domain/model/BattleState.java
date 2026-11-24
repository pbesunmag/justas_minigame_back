package com.jousting.rpg.domain.model;

/**
 * Representa el estado global de una batalla en memoria.
 */
public class BattleState {

    private int turnNumber;
    private final Knight knight1;
    private final Knight knight2;
    private boolean battleOver;
    private String winnerName;

    public BattleState(Knight knight1, Knight knight2) {
        this.turnNumber = 0;
        this.knight1 = knight1;
        this.knight2 = knight2;
        this.battleOver = false;
        this.winnerName = null;
    }

    public void advanceTurn() {
        this.turnNumber++;
    }

    public void finishBattle(String winnerName) {
        this.battleOver = true;
        this.winnerName = winnerName;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public Knight getKnight1() {
        return knight1;
    }

    public Knight getKnight2() {
        return knight2;
    }

    public boolean isBattleOver() {
        return battleOver;
    }

    public String getWinnerName() {
        return winnerName;
    }
}
