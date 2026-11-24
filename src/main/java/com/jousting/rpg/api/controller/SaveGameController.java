package com.jousting.rpg.api.controller;

import com.jousting.rpg.api.dto.CreateSaveRequest;
import com.jousting.rpg.api.dto.SaveGameSummaryDTO;
import com.jousting.rpg.domain.catalog.ConsumableItem;
import com.jousting.rpg.domain.model.Knight;
import com.jousting.rpg.domain.model.SaveGame;
import com.jousting.rpg.service.SaveGameService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de ranuras de guardado y perfiles del jugador.
 */
@RestController
@RequestMapping("/api/saves")
@Tag(name = "Partidas", description = "Gestión de ranuras de guardado y perfiles del caballero")
public class SaveGameController {

    private final SaveGameService saveGameService;

    public SaveGameController(SaveGameService saveGameService) {
        this.saveGameService = saveGameService;
    }

    @GetMapping
    public ResponseEntity<List<SaveGameSummaryDTO>> listSaves(@AuthenticationPrincipal UserDetails currentUser) {
        List<SaveGame> saves = saveGameService.getSavesByOwner(currentUser.getUsername());
        List<SaveGameSummaryDTO> dtos = saves.stream().map(this::mapToSummary).toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<SaveGameSummaryDTO> createSave(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestBody(required = false) CreateSaveRequest request
    ) {
        SaveGame saveGame = saveGameService.createNewGame(currentUser.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToSummary(saveGame));
    }

    @GetMapping("/{saveId}")
    public ResponseEntity<SaveGameSummaryDTO> getSaveDetail(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable Long saveId
    ) {
        SaveGame saveGame = saveGameService.getVerifiedSaveGame(saveId, currentUser.getUsername());
        return ResponseEntity.ok(mapToSummary(saveGame));
    }

    @DeleteMapping("/{saveId}")
    public ResponseEntity<Void> deleteSave(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable Long saveId
    ) {
        saveGameService.deleteSaveGame(saveId, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }

    private SaveGameSummaryDTO mapToSummary(SaveGame s) {
        Knight k = s.getProtagonist();
        Map<String, Integer> consumablesMap = new HashMap<>();
        if (k.getInventory() != null) {
            for (ConsumableItem item : k.getInventory()) {
                consumablesMap.merge(item.getName(), 1, Integer::sum);
            }
        }

        return new SaveGameSummaryDTO(
                s.getId(),
                k.getName(),
                s.getAge(),
                s.getBodyType(),
                s.getSkinTone(),
                s.getHairStyle(),
                s.getHairColor(),
                s.getHorseName(),
                s.getHorseColor(),
                k.getLevel(),
                k.getExperience(),
                k.getGold(),
                (int) k.getMaxHp(),
                k.getMaxStamina(),
                k.getMainWeapon() != null ? k.getMainWeapon().getId() : null,
                k.getSecondaryWeapon() != null ? k.getSecondaryWeapon().getId() : null,
                k.getArmor() != null ? k.getArmor().getId() : null,
                k.getHorse() != null ? k.getHorse().getId() : null,
                k.getDefeatedEnemies(),
                consumablesMap,
                s.getUpdatedAt()
        );
    }
}
