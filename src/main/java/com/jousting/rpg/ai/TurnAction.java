package com.jousting.rpg.ai;

import com.jousting.rpg.domain.catalog.ConsumableItem;
import com.jousting.rpg.domain.enums.HorseOrder;
import com.jousting.rpg.domain.enums.RiderAction;
import com.jousting.rpg.domain.enums.TargetZone;
import com.jousting.rpg.dto.PlayerTurnChoice;

/**
 * Acción y orden decidida por una IA o jugador para el turno actual.
 */
public class TurnAction extends PlayerTurnChoice {

    public TurnAction(RiderAction riderAction, TargetZone targetZone, 
                      HorseOrder horseOrder, ConsumableItem itemToUse) {
        super(riderAction, targetZone, horseOrder, itemToUse);
    }

    public static TurnAction attack(TargetZone zone, HorseOrder horseOrder) {
        return new TurnAction(RiderAction.ATTACK, zone, horseOrder, null);
    }

    public static TurnAction defend(HorseOrder horseOrder) {
        return new TurnAction(RiderAction.DEFEND, null, horseOrder, null);
    }

    public static TurnAction dodge(HorseOrder horseOrder) {
        return new TurnAction(RiderAction.DODGE, null, horseOrder, null);
    }

    public static TurnAction ultimate(TargetZone zone, HorseOrder horseOrder) {
        return new TurnAction(RiderAction.ULTIMATE, zone, horseOrder, null);
    }

    public static TurnAction useItem(ConsumableItem item, HorseOrder horseOrder) {
        return new TurnAction(RiderAction.USE_ITEM, null, horseOrder, item);
    }

    public static TurnAction switchWeapon(HorseOrder horseOrder) {
        return new TurnAction(RiderAction.SWITCH_WEAPON, null, horseOrder, null);
    }

    public static TurnAction surrender() {
        return new TurnAction(RiderAction.SURRENDER, null, HorseOrder.MAINTAIN, null);
    }
}
