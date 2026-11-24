package com.jousting.rpg.dto;

import com.jousting.rpg.domain.enums.ItemStatus;

/**
 * DTO que representa un objeto del catálogo en la armería con su estado actual para el jugador.
 */
public class ArmoryItemDto {

    private final String itemId;
    private final String name;
    private final String category;
    private final int price;
    private final int requiredLevel;
    private final String requiredBossDefeat;
    private final ItemStatus status;
    private final String lockReason;

    public ArmoryItemDto(String itemId, String name, String category, int price, 
                         int requiredLevel, String requiredBossDefeat, 
                         ItemStatus status, String lockReason) {
        this.itemId = itemId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.requiredLevel = requiredLevel;
        this.requiredBossDefeat = requiredBossDefeat;
        this.status = status;
        this.lockReason = lockReason;
    }

    public String getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getPrice() {
        return price;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public String getRequiredBossDefeat() {
        return requiredBossDefeat;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public String getLockReason() {
        return lockReason;
    }
}
