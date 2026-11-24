package com.jousting.rpg.domain.catalog;

import com.jousting.rpg.domain.enums.ConsumableType;

/**
 * Catálogo estático para objetos consumibles de inventario.
 */
public class ConsumableItem {

    private final String id;
    private final String name;
    private final ConsumableType type;
    private final int effectValue;

    public ConsumableItem(String id, String name, ConsumableType type, int effectValue) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.effectValue = effectValue;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ConsumableType getType() {
        return type;
    }

    public int getEffectValue() {
        return effectValue;
    }
}
