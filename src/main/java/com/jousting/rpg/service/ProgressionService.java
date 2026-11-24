package com.jousting.rpg.service;

import com.jousting.rpg.domain.catalog.Equipment;
import com.jousting.rpg.domain.catalog.EquipmentCatalog;
import com.jousting.rpg.domain.model.BattleReward;
import com.jousting.rpg.domain.model.Knight;
import com.jousting.rpg.dto.RewardSummary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio desacoplado que gestiona la ganancia de oro, experiencia,
 * subidas de nivel y desbloqueo progresivo del catálogo tras victorias.
 */
@Service
public class ProgressionService {

    /**
     * Otorga las recompensas de victoria al jugador, evalúa subida de nivel
     * y detecta nuevos objetos desbloqueados en la armería.
     */
    public RewardSummary grantVictoryRewards(Knight player, Knight defeatedEnemy) {
        BattleReward reward = defeatedEnemy.getBattleReward();
        List<String> logs = new ArrayList<>();

        if (reward == null) {
            logs.add("El oponente derrotado no poseía botín registrado.");
            return new RewardSummary(0, 0, false, false, player.getLevel(), List.of(), logs);
        }

        String enemyCode = reward.getEnemyCode();
        boolean firstVictory = !player.isEnemyDefeated(enemyCode);

        // Capturar los objetos bloqueados antes de aplicar recompensas
        Set<String> previouslyLockedItemIds = EquipmentCatalog.getAllEquipment().stream()
                .filter(eq -> isItemLocked(player, eq))
                .map(Equipment::getId)
                .collect(Collectors.toSet());

        int goldEarned;
        int xpEarned;

        if (firstVictory) {
            goldEarned = reward.getGold();
            xpEarned = reward.getExperience();
            player.recordDefeatedEnemy(enemyCode);
            logs.add(String.format("¡PRIMERA VICTORIA contra %s! Has obtenido el botín completo: +%d de oro y +%d XP.",
                    defeatedEnemy.getName(), goldEarned, xpEarned));
        } else {
            // Repetición: 25% de recompensas
            goldEarned = Math.max(1, (int) Math.round(reward.getGold() * 0.25));
            xpEarned = Math.max(1, (int) Math.round(reward.getExperience() * 0.25));
            logs.add(String.format("Victoria repetida contra %s. Recompensa reducida (25%%): +%d de oro y +%d XP.",
                    defeatedEnemy.getName(), goldEarned, xpEarned));
        }

        player.addGold(goldEarned);
        player.addExperience(xpEarned);

        // Evaluación de subida de nivel automática (umbral: level * 100 XP)
        boolean leveledUp = false;
        while (player.getExperience() >= player.getLevel() * 100) {
            int threshold = player.getLevel() * 100;
            player.setExperience(player.getExperience() - threshold);
            player.levelUp();
            leveledUp = true;
            logs.add(String.format("¡¡SUBIDA DE NIVEL!! %s ha alcanzado el Nivel %d (+15 Salud Máx, +10 Stamina Máx).",
                    player.getName(), player.getLevel()));
        }

        // Comprobar qué objetos antes bloqueados son ahora comprables
        List<String> newlyAvailableItems = new ArrayList<>();
        for (Equipment eq : EquipmentCatalog.getAllEquipment()) {
            if (previouslyLockedItemIds.contains(eq.getId()) && !isItemLocked(player, eq)) {
                newlyAvailableItems.add(eq.getName());
                logs.add(String.format("¡NUEVO EQUIPO DISPONIBLE EN LA ARMERÍA! '%s' (Precio: %d oro).",
                        eq.getName(), eq.getPrice()));
            }
        }

        return new RewardSummary(goldEarned, xpEarned, firstVictory, leveledUp, 
                player.getLevel(), newlyAvailableItems, logs);
    }

    private boolean isItemLocked(Knight player, Equipment eq) {
        boolean levelMet = player.getLevel() >= eq.getRequiredLevel();
        boolean bossMet = (eq.getRequiredBossDefeat() == null) || player.isEnemyDefeated(eq.getRequiredBossDefeat());
        return !levelMet || !bossMet;
    }
}
