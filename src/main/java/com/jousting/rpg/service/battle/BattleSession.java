package com.jousting.rpg.service.battle;

import com.jousting.rpg.ai.CombatAIStrategy;
import com.jousting.rpg.domain.model.BattleState;
import com.jousting.rpg.domain.model.Knight;
import com.jousting.rpg.dto.RewardSummary;

import java.util.Collections;
import java.util.List;

/**
 * Mantiene el estado en memoria de una sesión de combate activa.
 */
public class BattleSession {

    private final String battleId;
    private final Long saveId;
    private final String ownerUsername;
    private final BattleState battleState;
    private final Knight playerKnight;
    private final Knight enemyKnight;
    private final CombatAIStrategy aiStrategy;
    private final String enemyCode;
    private String status; // "IN_PROGRESS", "FINISHED"
    private List<String> latestNarrative;
    private RewardSummary rewardSummary;

    public BattleSession(String battleId, Long saveId, String ownerUsername, 
                         BattleState battleState, Knight playerKnight, 
                         Knight enemyKnight, CombatAIStrategy aiStrategy, String enemyCode) {
        this.battleId = battleId;
        this.saveId = saveId;
        this.ownerUsername = ownerUsername;
        this.battleState = battleState;
        this.playerKnight = playerKnight;
        this.enemyKnight = enemyKnight;
        this.aiStrategy = aiStrategy != null ? aiStrategy : (enemyKnight != null ? enemyKnight.getAiStrategy() : null);
        this.enemyCode = enemyCode;
        this.status = "IN_PROGRESS";
        this.latestNarrative = Collections.emptyList();
        this.rewardSummary = null;
    }

    public BattleSession(String battleId, Long saveId, String ownerUsername, 
                         BattleState battleState, Knight playerKnight, 
                         Knight enemyKnight, String enemyCode) {
        this(battleId, saveId, ownerUsername, battleState, playerKnight, enemyKnight, 
             enemyKnight != null ? enemyKnight.getAiStrategy() : null, enemyCode);
    }

    public String getBattleId() {
        return battleId;
    }

    public Long getSaveId() {
        return saveId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public BattleState getBattleState() {
        return battleState;
    }

    public Knight getPlayerKnight() {
        return playerKnight;
    }

    public Knight getEnemyKnight() {
        return enemyKnight;
    }

    public CombatAIStrategy getAiStrategy() {
        return aiStrategy;
    }

    public String getEnemyCode() {
        return enemyCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getLatestNarrative() {
        return latestNarrative;
    }

    public void setLatestNarrative(List<String> latestNarrative) {
        this.latestNarrative = latestNarrative != null ? latestNarrative : Collections.emptyList();
    }

    public RewardSummary getRewardSummary() {
        return rewardSummary;
    }

    public void setRewardSummary(RewardSummary rewardSummary) {
        this.rewardSummary = rewardSummary;
    }
}
