package com.jousting.rpg.service;

import com.jousting.rpg.domain.catalog.Armor;
import com.jousting.rpg.domain.catalog.Equipment;
import com.jousting.rpg.domain.catalog.EquipmentCatalog;
import com.jousting.rpg.domain.catalog.Horse;
import com.jousting.rpg.domain.catalog.Weapon;
import com.jousting.rpg.domain.enums.ItemStatus;
import com.jousting.rpg.domain.model.Knight;
import com.jousting.rpg.dto.ArmoryItemDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio desacoplado que gestiona la armería: consulta de catálogo con estados,
 * compra de equipamiento y equipación de piezas en el caballero.
 */
@Service
public class ArmoryService {

    /**
     * Obtiene el catálogo completo de la armería reflejando el estado individual para el jugador.
     */
    public List<ArmoryItemDto> getCatalog(Knight player) {
        List<ArmoryItemDto> catalogDtos = new ArrayList<>();

        for (Equipment eq : EquipmentCatalog.getAllEquipment()) {
            ItemStatus status;
            String lockReason = null;

            if (isEquipped(player, eq)) {
                status = ItemStatus.EQUIPPED;
            } else if (player.hasItemUnlocked(eq.getId())) {
                status = ItemStatus.OWNED;
            } else {
                boolean levelMet = player.getLevel() >= eq.getRequiredLevel();
                boolean bossMet = (eq.getRequiredBossDefeat() == null) || player.isEnemyDefeated(eq.getRequiredBossDefeat());

                if (levelMet && bossMet) {
                    status = ItemStatus.AVAILABLE;
                } else {
                    status = ItemStatus.LOCKED;
                    if (!levelMet && !bossMet) {
                        lockReason = String.format("Requiere Nivel %d y derrotar a %s", 
                                eq.getRequiredLevel(), eq.getRequiredBossDefeat());
                    } else if (!levelMet) {
                        lockReason = String.format("Requiere Nivel %d", eq.getRequiredLevel());
                    } else {
                        lockReason = String.format("Requiere derrotar a %s", eq.getRequiredBossDefeat());
                    }
                }
            }

            String category = determineCategory(eq);
            catalogDtos.add(new ArmoryItemDto(
                    eq.getId(), eq.getName(), category, eq.getPrice(),
                    eq.getRequiredLevel(), eq.getRequiredBossDefeat(),
                    status, lockReason
            ));
        }

        return catalogDtos;
    }

    /**
     * Valida los requisitos y efectúa la compra de un objeto con el oro del jugador.
     */
    public boolean buyEquipment(Knight player, String itemId) {
        Optional<Equipment> optEq = EquipmentCatalog.findById(itemId);
        if (optEq.isEmpty()) {
            return false;
        }

        Equipment eq = optEq.get();

        // Si ya lo tiene desbloqueado o equipado, no se vuelve a comprar
        if (player.hasItemUnlocked(eq.getId())) {
            return false;
        }

        // Validación de nivel
        if (player.getLevel() < eq.getRequiredLevel()) {
            return false;
        }

        // Validación de jefe derrotado
        if (eq.getRequiredBossDefeat() != null && !player.isEnemyDefeated(eq.getRequiredBossDefeat())) {
            return false;
        }

        // Validación de fondos suficientes
        if (!player.spendGold(eq.getPrice())) {
            return false;
        }

        // Registro de compra exitosa
        player.unlockItem(eq.getId());
        return true;
    }

    /**
     * Equipa una pieza previamente adquirida en la ranura correspondiente del jugador.
     */
    public boolean equipItem(Knight player, String itemId) {
        if (!player.hasItemUnlocked(itemId)) {
            return false;
        }

        Optional<Equipment> optEq = EquipmentCatalog.findById(itemId);
        if (optEq.isEmpty()) {
            return false;
        }

        Equipment eq = optEq.get();

        if (eq instanceof Weapon weapon) {
            player.setMainWeapon(weapon);
            player.setEquippedWeapon(weapon);
            return true;
        } else if (eq instanceof Armor armor) {
            player.setArmor(armor);
            return true;
        } else if (eq instanceof Horse horse) {
            player.setHorse(horse);
            return true;
        }

        return false;
    }

    private boolean isEquipped(Knight player, Equipment eq) {
        String id = eq.getId();
        if (player.getMainWeapon() != null && player.getMainWeapon().getId().equalsIgnoreCase(id)) {
            return true;
        }
        if (player.getSecondaryWeapon() != null && player.getSecondaryWeapon().getId().equalsIgnoreCase(id)) {
            return true;
        }
        if (player.getArmor() != null && player.getArmor().getId().equalsIgnoreCase(id)) {
            return true;
        }
        if (player.getHorse() != null && player.getHorse().getId().equalsIgnoreCase(id)) {
            return true;
        }
        return false;
    }

    private String determineCategory(Equipment eq) {
        if (eq instanceof Weapon) return "WEAPON";
        if (eq instanceof Armor) return "ARMOR";
        if (eq instanceof Horse) return "HORSE";
        return "MISC";
    }
}
