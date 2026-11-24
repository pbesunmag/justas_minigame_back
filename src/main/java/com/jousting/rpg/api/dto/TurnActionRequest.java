package com.jousting.rpg.api.dto;

import com.jousting.rpg.domain.enums.HorseOrder;
import com.jousting.rpg.domain.enums.RiderAction;
import com.jousting.rpg.domain.enums.TargetZone;

/**
 * Petición para ejecutar la acción de jinete y orden a la montura en el turno actual.
 */
public record TurnActionRequest(
        RiderAction riderAction,
        TargetZone targetZone,
        HorseOrder horseOrder,
        String itemId
) {
}
