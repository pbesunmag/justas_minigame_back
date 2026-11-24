package com.jousting.rpg.service.battle;

import com.jousting.rpg.ai.TurnAction;
import com.jousting.rpg.api.dto.BattleResponseDTO;
import com.jousting.rpg.api.dto.KnightBattleStatusDTO;
import com.jousting.rpg.api.dto.TurnActionRequest;
import com.jousting.rpg.domain.catalog.ConsumableItem;
import com.jousting.rpg.domain.enums.HorseOrder;
import com.jousting.rpg.domain.enums.RiderAction;
import com.jousting.rpg.domain.enums.TargetZone;
import com.jousting.rpg.domain.factory.KnightFactory;
import com.jousting.rpg.domain.model.BattleState;
import com.jousting.rpg.domain.model.Knight;
import com.jousting.rpg.domain.model.SaveGame;
import com.jousting.rpg.dto.PlayerTurnChoice;
import com.jousting.rpg.dto.RewardSummary;
import com.jousting.rpg.dto.TurnResult;
import com.jousting.rpg.service.CassianAdvisorService;
import com.jousting.rpg.service.CombatEngineService;
import com.jousting.rpg.service.ProgressionService;
import com.jousting.rpg.service.SaveGameService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor en memoria de sesiones de combate activas y orquestador del ciclo de vida de la batalla.
 */
@Service
public class ActiveBattleManager {

    private final Map<String, BattleSession> activeBattles = new ConcurrentHashMap<>();
    private final CombatEngineService combatEngineService;
    private final ProgressionService progressionService;
    private final SaveGameService saveGameService;
    private final CassianAdvisorService cassianAdvisorService;

    public ActiveBattleManager(CombatEngineService combatEngineService, 
                               ProgressionService progressionService, 
                               SaveGameService saveGameService,
                               CassianAdvisorService cassianAdvisorService) {
        this.combatEngineService = combatEngineService;
        this.progressionService = progressionService;
        this.saveGameService = saveGameService;
        this.cassianAdvisorService = cassianAdvisorService;
    }

    /**
     * Inicia una nueva batalla vinculada a la partida y al usuario autenticado.
     */
    public BattleSession startBattle(Long saveId, String ownerUsername, Knight playerKnight, String enemyCode) {
        Knight enemy = switch (enemyCode.toUpperCase()) {
            case "KNIGHT_1", "GIDEON" -> KnightFactory.createKnightOne();
            case "KNIGHT_2", "VALERIE" -> KnightFactory.createKnightTwo();
            case "ERK" -> KnightFactory.createErk();
            default -> throw new IllegalArgumentException("Código de enemigo inválido: " + enemyCode + 
                    ". Opciones disponibles: KNIGHT_1, KNIGHT_2, ERK");
        };

        playerKnight.resetTurnState();
        enemy.resetTurnState();

        String battleId = UUID.randomUUID().toString();
        BattleState battleState = new BattleState(playerKnight, enemy);
        BattleSession session = new BattleSession(battleId, saveId, ownerUsername, battleState, playerKnight, enemy, enemy.getAiStrategy(), enemyCode);

        session.setLatestNarrative(List.of(
                String.format("¡Comienza la justa en la arena real! %s se enfrenta a %s.", 
                        playerKnight.getName(), enemy.getName())
        ));

        activeBattles.put(battleId, session);
        return session;
    }

    /**
     * Obtiene y valida la titularidad de una sesión de combate activa.
     */
    public BattleSession getVerifiedSession(String battleId, Long saveId, String ownerUsername) {
        BattleSession session = activeBattles.get(battleId);
        if (session == null) {
            throw new NoSuchElementException("No existe ninguna batalla activa con ID: " + battleId);
        }

        if (!session.getSaveId().equals(saveId)) {
            throw new AccessDeniedException("Acceso denegado: La batalla no pertenece a la partida indicada.");
        }

        if (!session.getOwnerUsername().equalsIgnoreCase(ownerUsername)) {
            throw new AccessDeniedException("Acceso denegado: No tienes autorización para acceder a esta batalla.");
        }

        return session;
    }

    /**
     * Ejecuta un turno simultáneo de combate y procesa recompensas si el jugador gana.
     */
    public BattleResponseDTO executeTurn(String battleId, Long saveId, String ownerUsername, 
                                        TurnActionRequest request, SaveGame saveGame) {
        BattleSession session = getVerifiedSession(battleId, saveId, ownerUsername);

        if ("FINISHED".equals(session.getStatus()) || !"IN_PROGRESS".equals(session.getStatus())) {
            throw new IllegalStateException("El combate ya ha finalizado con resultado: " + session.getStatus());
        }

        // Construir la acción del jugador
        ConsumableItem itemToUse = null;
        if (request != null && request.itemId() != null && !request.itemId().isBlank()) {
            itemToUse = session.getPlayerKnight().getInventory().stream()
                    .filter(item -> item.getId().equalsIgnoreCase(request.itemId()))
                    .findFirst()
                    .orElse(null);
        }

        PlayerTurnChoice playerChoice = new PlayerTurnChoice(
                (request != null && request.riderAction() != null) ? request.riderAction() : RiderAction.ATTACK,
                (request != null && request.targetZone() != null) ? request.targetZone() : TargetZone.CHEST,
                (request != null && request.horseOrder() != null) ? request.horseOrder() : HorseOrder.MAINTAIN,
                itemToUse
        );

        // Decisión de la IA enemiga mediante su CombatAIStrategy
        TurnAction enemyAction = null;
        if (session.getAiStrategy() != null) {
            enemyAction = session.getAiStrategy().decideTurn(
                    session.getEnemyKnight(),
                    session.getPlayerKnight(),
                    session.getBattleState()
            );
        } else {
            enemyAction = session.getEnemyKnight().decideTurn(
                    session.getPlayerKnight(),
                    session.getBattleState()
            );
        }
        if (enemyAction == null) {
            enemyAction = TurnAction.attack(TargetZone.CHEST, HorseOrder.MAINTAIN);
        }

        // Resolver turno en el motor de combate
        TurnResult turnResult = combatEngineService.resolveTurn(
                session.getBattleState(), 
                playerChoice, 
                enemyAction
        );

        session.setLatestNarrative(turnResult.getLogs());

        // Comprobar finalización
        if (turnResult.isBattleOver()) {
            session.setStatus("FINISHED");
            String winner = turnResult.getWinnerName();
            if (session.getPlayerKnight().getName().equalsIgnoreCase(winner)) {
                // Conceder botín y subida de nivel
                RewardSummary reward = progressionService.grantVictoryRewards(
                        session.getPlayerKnight(), 
                        session.getEnemyKnight()
                );
                session.setRewardSummary(reward);
                // Guardar progreso en el repositorio
                saveGameService.updateSaveGame(saveGame);
            }
        }

        return toResponseDTO(session);
    }

    public BattleResponseDTO toResponseDTO(BattleSession session) {
        String cassianTip = cassianAdvisorService.generateTip(
                session.getBattleState(),
                session.getPlayerKnight(),
                session.getEnemyKnight()
        );
        return new BattleResponseDTO(
                session.getBattleId(),
                session.getBattleState().getTurnNumber(),
                session.getStatus(),
                toStatusDTO(session.getPlayerKnight()),
                toStatusDTO(session.getEnemyKnight()),
                session.getLatestNarrative(),
                session.getRewardSummary(),
                cassianTip
        );
    }

    private KnightBattleStatusDTO toStatusDTO(Knight k) {
        return new KnightBattleStatusDTO(
                k.getName(),
                k.getCurrentHp(),
                k.getMaxHp(),
                k.getCurrentStamina(),
                k.getMaxStamina(),
                k.getCurrentShieldHp(),
                k.getMaxShieldHp(),
                k.isShieldBroken(),
                k.getUltimateCharge(),
                k.getCurrentSpeed(),
                k.getCurrentFatigue(),
                k.isHorsePanicked(),
                k.getEquippedWeapon() != null ? k.getEquippedWeapon().getName() : "Desarmado"
        );
    }
}
