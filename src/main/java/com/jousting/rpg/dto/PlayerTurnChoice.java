package com.jousting.rpg.dto;

import com.jousting.rpg.domain.catalog.ConsumableItem;
import com.jousting.rpg.domain.enums.HorseOrder;
import com.jousting.rpg.domain.enums.RiderAction;
import com.jousting.rpg.domain.enums.TargetZone;

/**
 * Elección de acción simultánea seleccionada por un jugador para el turno actual.
 */
public class PlayerTurnChoice {

    private final RiderAction riderAction;
    private final TargetZone targetZone;
    private final HorseOrder horseOrder;
    private final ConsumableItem itemToUse;

    public PlayerTurnChoice(RiderAction riderAction, TargetZone targetZone, 
                            HorseOrder horseOrder, ConsumableItem itemToUse) {
        this.riderAction = riderAction;
        this.targetZone = targetZone;
        this.horseOrder = horseOrder;
        this.itemToUse = itemToUse;
    }

    public static PlayerTurnChoice attack(TargetZone zone, HorseOrder horseOrder) {
        return new PlayerTurnChoice(RiderAction.ATTACK, zone, horseOrder, null);
    }

    public static PlayerTurnChoice defend(HorseOrder horseOrder) {
        return new PlayerTurnChoice(RiderAction.DEFEND, null, horseOrder, null);
    }

    public static PlayerTurnChoice dodge(HorseOrder horseOrder) {
        return new PlayerTurnChoice(RiderAction.DODGE, null, horseOrder, null);
    }

    public static PlayerTurnChoice ultimate(TargetZone zone, HorseOrder horseOrder) {
        return new PlayerTurnChoice(RiderAction.ULTIMATE, zone, horseOrder, null);
    }

    public static PlayerTurnChoice useItem(ConsumableItem item, HorseOrder horseOrder) {
        return new PlayerTurnChoice(RiderAction.USE_ITEM, null, horseOrder, item);
    }

    public static PlayerTurnChoice switchWeapon(HorseOrder horseOrder) {
        return new PlayerTurnChoice(RiderAction.SWITCH_WEAPON, null, horseOrder, null);
    }

    public static PlayerTurnChoice surrender() {
        return new PlayerTurnChoice(RiderAction.SURRENDER, null, HorseOrder.MAINTAIN, null);
    }

    public RiderAction getRiderAction() {
        return riderAction;
    }

    public TargetZone getTargetZone() {
        return targetZone;
    }

    public HorseOrder getHorseOrder() {
        return horseOrder;
    }

    public ConsumableItem getItemToUse() {
        return itemToUse;
    }
}
