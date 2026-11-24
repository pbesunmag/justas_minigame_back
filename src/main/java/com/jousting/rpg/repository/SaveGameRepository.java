package com.jousting.rpg.repository;

import com.jousting.rpg.domain.model.SaveGame;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Repositorio en memoria para el almacenamiento seguro de partidas guardadas.
 */
@Repository
public class SaveGameRepository {

    private final Map<Long, SaveGame> savesById = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public SaveGame save(SaveGame saveGame) {
        if (saveGame.getId() == null) {
            saveGame.setId(idGenerator.getAndIncrement());
        }
        savesById.put(saveGame.getId(), saveGame);
        return saveGame;
    }

    public Optional<SaveGame> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(savesById.get(id));
    }

    public List<SaveGame> findByOwnerUsername(String ownerUsername) {
        if (ownerUsername == null) {
            return List.of();
        }
        return savesById.values().stream()
                .filter(save -> save.getOwnerUsername().equalsIgnoreCase(ownerUsername))
                .toList();
    }

    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }
        return savesById.remove(id) != null;
    }

    public void clear() {
        savesById.clear();
    }
}
