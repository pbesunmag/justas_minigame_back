package com.jousting.rpg.service;

import com.jousting.rpg.api.dto.CreateSaveRequest;
import com.jousting.rpg.domain.catalog.Armor;
import com.jousting.rpg.domain.catalog.Equipment;
import com.jousting.rpg.domain.catalog.EquipmentCatalog;
import com.jousting.rpg.domain.catalog.Horse;
import com.jousting.rpg.domain.factory.KnightFactory;
import com.jousting.rpg.domain.model.Knight;
import com.jousting.rpg.domain.model.SaveGame;
import com.jousting.rpg.repository.SaveGameRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Servicio de gestión de partidas guardadas con verificación estricta de propiedad de usuario.
 */
@Service
public class SaveGameService {

    private final SaveGameRepository saveGameRepository;

    public SaveGameService(SaveGameRepository saveGameRepository) {
        this.saveGameRepository = saveGameRepository;
    }

    /**
     * Crea una nueva partida personalizada desbloqueando el triángulo básico de 3 armas
     * (BASIC_SWORD, BASIC_LANCE, BASIC_MACE) y equipando la armadura y raza de caballo elegidas.
     */
    public SaveGame createNewGame(String authenticatedUsername, CreateSaveRequest request) {
        if (authenticatedUsername == null || authenticatedUsername.isBlank()) {
            throw new IllegalArgumentException("El usuario propietario es obligatorio.");
        }

        String knightName = (request != null && request.knightName() != null && !request.knightName().isBlank())
                ? request.knightName().trim()
                : authenticatedUsername;

        Knight protagonist = KnightFactory.createProtagonist(knightName);

        // Desbloquear por defecto las 3 armas básicas del triángulo táctico
        protagonist.unlockItem(EquipmentCatalog.BASIC_SWORD.getId());
        protagonist.unlockItem(EquipmentCatalog.BASIC_LANCE.getId());
        protagonist.unlockItem(EquipmentCatalog.BASIC_MACE.getId());

        // Equipar lanza y espada básicas como configuración inicial
        protagonist.setMainWeapon(EquipmentCatalog.BASIC_LANCE);
        protagonist.setSecondaryWeapon(EquipmentCatalog.BASIC_SWORD);
        protagonist.setEquippedWeapon(EquipmentCatalog.BASIC_LANCE);

        // Aplicar armadura inicial seleccionada si es válida
        if (request != null && request.initialArmorId() != null && !request.initialArmorId().isBlank()) {
            Optional<Equipment> armorOpt = EquipmentCatalog.findById(request.initialArmorId().trim());
            if (armorOpt.isPresent() && armorOpt.get() instanceof Armor selectedArmor) {
                protagonist.unlockItem(selectedArmor.getId());
                protagonist.setArmor(selectedArmor);
            }
        }

        // Aplicar raza de caballo inicial seleccionada si es válida
        if (request != null && request.initialHorseId() != null && !request.initialHorseId().isBlank()) {
            Optional<Equipment> horseOpt = EquipmentCatalog.findById(request.initialHorseId().trim());
            if (horseOpt.isPresent() && horseOpt.get() instanceof Horse selectedHorse) {
                protagonist.unlockItem(selectedHorse.getId());
                protagonist.setHorse(selectedHorse);
            }
        }

        int age = (request != null && request.age() != null && request.age() >= 16 && request.age() <= 80)
                ? request.age()
                : 22;
        String bodyType = defaultIfBlank(request != null ? request.bodyType() : null, "ATHLETIC");
        String skinTone = defaultIfBlank(request != null ? request.skinTone() : null, "LIGHT");
        String hairStyle = defaultIfBlank(request != null ? request.hairStyle() : null, "SHORT");
        String hairColor = defaultIfBlank(request != null ? request.hairColor() : null, "BROWN");
        String horseName = defaultIfBlank(request != null ? request.horseName() : null, "Rocín");
        String horseColor = defaultIfBlank(request != null ? request.horseColor() : null, "BAY");

        SaveGame saveGame = new SaveGame(
                authenticatedUsername,
                "Aventura de " + knightName,
                protagonist,
                age,
                bodyType,
                skinTone,
                hairStyle,
                hairColor,
                horseName,
                horseColor
        );

        return saveGameRepository.save(saveGame);
    }

    /**
     * Guarda o actualiza una partida vinculándola a la cuenta del usuario autenticado.
     */
    public SaveGame createSaveGame(String authenticatedUsername, String saveName, Knight protagonist) {
        if (authenticatedUsername == null || authenticatedUsername.isBlank()) {
            throw new IllegalArgumentException("El usuario propietario es obligatorio.");
        }
        SaveGame saveGame = new SaveGame(authenticatedUsername, saveName, protagonist);
        return saveGameRepository.save(saveGame);
    }

    /**
     * Obtiene una partida guardada verificando que pertenezca al usuario del token JWT autenticado.
     * Si pertenece a otro jugador, deniega el acceso lanzando AccessDeniedException.
     */
    public SaveGame getVerifiedSaveGame(Long saveId, String authenticatedUsername) {
        SaveGame saveGame = saveGameRepository.findById(saveId)
                .orElseThrow(() -> new NoSuchElementException("No se encontró ninguna partida con ID: " + saveId));

        if (!saveGame.getOwnerUsername().equalsIgnoreCase(authenticatedUsername)) {
            throw new AccessDeniedException("Acceso denegado: No tienes autorización para acceder a la partida de otro caballero.");
        }

        return saveGame;
    }

    /**
     * Lista todas las partidas guardadas del usuario autenticado.
     */
    public List<SaveGame> getSavesByOwner(String authenticatedUsername) {
        return saveGameRepository.findByOwnerUsername(authenticatedUsername);
    }

    /**
     * Actualiza y persiste el estado de una partida existente.
     */
    public SaveGame updateSaveGame(SaveGame saveGame) {
        return saveGameRepository.save(saveGame);
    }

    /**
     * Elimina una partida guardada comprobando previamente la titularidad del usuario.
     */
    public boolean deleteSaveGame(Long saveId, String authenticatedUsername) {
        getVerifiedSaveGame(saveId, authenticatedUsername);
        return saveGameRepository.deleteById(saveId);
    }

    private String defaultIfBlank(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value.trim() : fallback;
    }
}
