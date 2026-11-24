package com.jousting.rpg.api.controller;

import com.jousting.rpg.api.dto.BattleResponseDTO;
import com.jousting.rpg.api.dto.StartBattleRequest;
import com.jousting.rpg.api.dto.TurnActionRequest;
import com.jousting.rpg.domain.model.SaveGame;
import com.jousting.rpg.service.SaveGameService;
import com.jousting.rpg.service.battle.ActiveBattleManager;
import com.jousting.rpg.service.battle.BattleSession;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para iniciar y disputar combates por turnos en tiempo real.
 */
@RestController
@RequestMapping("/api/saves/{saveId}/battles")
@Tag(name = "Combate", description = "Inicio y resolución por turnos de justas contra enemigos")
public class BattleController {

    private final ActiveBattleManager activeBattleManager;
    private final SaveGameService saveGameService;

    public BattleController(ActiveBattleManager activeBattleManager, SaveGameService saveGameService) {
        this.activeBattleManager = activeBattleManager;
        this.saveGameService = saveGameService;
    }

    @PostMapping("/start")
    public ResponseEntity<BattleResponseDTO> startBattle(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable Long saveId,
            @RequestBody(required = false) StartBattleRequest request
    ) {
        SaveGame saveGame = saveGameService.getVerifiedSaveGame(saveId, currentUser.getUsername());

        String enemyCode = (request != null && request.enemyCode() != null) ? request.enemyCode() : "KNIGHT_1";
        BattleSession session = activeBattleManager.startBattle(
                saveId, 
                currentUser.getUsername(), 
                saveGame.getProtagonist(), 
                enemyCode
        );

        return ResponseEntity.ok(activeBattleManager.toResponseDTO(session));
    }

    @PostMapping("/{battleId}/turn")
    public ResponseEntity<BattleResponseDTO> executeTurn(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable Long saveId,
            @PathVariable String battleId,
            @RequestBody(required = false) TurnActionRequest request
    ) {
        SaveGame saveGame = saveGameService.getVerifiedSaveGame(saveId, currentUser.getUsername());

        BattleResponseDTO response = activeBattleManager.executeTurn(
                battleId, 
                saveId, 
                currentUser.getUsername(), 
                request, 
                saveGame
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{battleId}")
    public ResponseEntity<BattleResponseDTO> getBattleState(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable Long saveId,
            @PathVariable String battleId
    ) {
        saveGameService.getVerifiedSaveGame(saveId, currentUser.getUsername());
        BattleSession session = activeBattleManager.getVerifiedSession(battleId, saveId, currentUser.getUsername());
        return ResponseEntity.ok(activeBattleManager.toResponseDTO(session));
    }
}
