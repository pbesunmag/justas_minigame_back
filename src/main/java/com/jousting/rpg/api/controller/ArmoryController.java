package com.jousting.rpg.api.controller;

import com.jousting.rpg.api.dto.ArmoryCatalogResponse;
import com.jousting.rpg.api.dto.ArmoryItemDTO;
import com.jousting.rpg.domain.catalog.Armor;
import com.jousting.rpg.domain.catalog.Equipment;
import com.jousting.rpg.domain.catalog.EquipmentCatalog;
import com.jousting.rpg.domain.catalog.Horse;
import com.jousting.rpg.domain.catalog.Weapon;
import com.jousting.rpg.domain.model.Knight;
import com.jousting.rpg.domain.model.SaveGame;
import com.jousting.rpg.dto.ArmoryItemDto;
import com.jousting.rpg.service.ArmoryService;
import com.jousting.rpg.service.SaveGameService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para consultar catálogo, comprar y equipar armamento en la partida.
 */
@RestController
@RequestMapping("/api/saves/{saveId}/armory")
@Tag(name = "Armería", description = "Consulta del catálogo, compra y equipación de armamento y monturas")
public class ArmoryController {

    private final ArmoryService armoryService;
    private final SaveGameService saveGameService;

    public ArmoryController(ArmoryService armoryService, SaveGameService saveGameService) {
        this.armoryService = armoryService;
        this.saveGameService = saveGameService;
    }

    @GetMapping
    public ResponseEntity<ArmoryCatalogResponse> getCatalog(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable Long saveId
    ) {
        SaveGame saveGame = saveGameService.getVerifiedSaveGame(saveId, currentUser.getUsername());
        Knight player = saveGame.getProtagonist();

        List<ArmoryItemDto> rawCatalog = armoryService.getCatalog(player);

        List<ArmoryItemDTO> weapons = new ArrayList<>();
        List<ArmoryItemDTO> armors = new ArrayList<>();
        List<ArmoryItemDTO> horses = new ArrayList<>();

        for (ArmoryItemDto item : rawCatalog) {
            Equipment eq = EquipmentCatalog.findById(item.getItemId()).orElse(null);
            Map<String, Object> stats = extractStats(eq);

            ArmoryItemDTO dto = new ArmoryItemDTO(
                    item.getItemId(),
                    item.getName(),
                    stats,
                    item.getPrice(),
                    item.getRequiredLevel(),
                    item.getRequiredBossDefeat(),
                    item.getStatus(),
                    item.getLockReason()
            );

            switch (item.getCategory()) {
                case "WEAPON" -> weapons.add(dto);
                case "ARMOR" -> armors.add(dto);
                case "HORSE" -> horses.add(dto);
            }
        }

        return ResponseEntity.ok(new ArmoryCatalogResponse(weapons, armors, horses));
    }

    @PostMapping("/buy/{itemId}")
    public ResponseEntity<?> buyItem(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable Long saveId,
            @PathVariable String itemId
    ) {
        SaveGame saveGame = saveGameService.getVerifiedSaveGame(saveId, currentUser.getUsername());
        Knight player = saveGame.getProtagonist();

        boolean success = armoryService.buyEquipment(player, itemId);
        if (!success) {
            throw new IllegalArgumentException("No se pudo comprar el objeto: fondos insuficientes, requisitos no cumplidos o ya adquirido.");
        }

        saveGameService.updateSaveGame(saveGame);
        return ResponseEntity.ok(Map.of(
                "message", "Objeto adquirido con éxito",
                "goldRemaining", player.getGold()
        ));
    }

    @PostMapping("/equip/{itemId}")
    public ResponseEntity<?> equipItem(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable Long saveId,
            @PathVariable String itemId
    ) {
        SaveGame saveGame = saveGameService.getVerifiedSaveGame(saveId, currentUser.getUsername());
        Knight player = saveGame.getProtagonist();

        boolean success = armoryService.equipItem(player, itemId);
        if (!success) {
            throw new IllegalArgumentException("No se pudo equipar el objeto: asegúrate de haberlo comprado previamente.");
        }

        saveGameService.updateSaveGame(saveGame);
        return ResponseEntity.ok(Map.of(
                "message", "Objeto equipado correctamente",
                "equippedWeapon", player.getEquippedWeapon().getName()
        ));
    }

    private Map<String, Object> extractStats(Equipment eq) {
        Map<String, Object> stats = new HashMap<>();
        if (eq instanceof Weapon w) {
            stats.put("damage", w.getBaseDamage());
            stats.put("shieldMultiplier", w.getShieldDamageMultiplier());
            stats.put("staminaCost", w.getStaminaCost());
            stats.put("type", w.getType().name());
        } else if (eq instanceof Armor a) {
            stats.put("headDefense", a.getHeadDefense());
            stats.put("chestDefense", a.getChestDefense());
            stats.put("weight", a.getWeight());
            stats.put("affinity", a.getAffinity().name());
        } else if (eq instanceof Horse h) {
            stats.put("maxSpeed", h.getMaxSpeed());
            stats.put("maxFatigue", h.getMaxFatigue());
            stats.put("temperament", h.getTemperament().name());
        }
        return stats;
    }
}
