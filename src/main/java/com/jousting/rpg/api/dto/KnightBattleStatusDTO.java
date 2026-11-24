package com.jousting.rpg.api.dto;

/**
 * Estado dinámico de un caballero durante el combate para actualización en el frontend.
 */
public record KnightBattleStatusDTO(
        String name,
        double currentHp,
        double maxHp,
        int currentStamina,
        int maxStamina,
        double currentShieldHp,
        double maxShieldHp,
        boolean shieldBroken,
        double ultimateCharge,
        int currentSpeed,
        int currentFatigue,
        boolean horsePanicked,
        String activeWeapon
) {
    public String equippedWeaponName() {
        return activeWeapon;
    }
}
