package com.jousting.rpg.domain.factory;

import com.jousting.rpg.ai.AgileDuelistStrategy;
import com.jousting.rpg.ai.DefensiveTankStrategy;
import com.jousting.rpg.ai.SteppeMasterStrategy;
import com.jousting.rpg.domain.catalog.EquipmentCatalog;
import com.jousting.rpg.domain.model.BattleReward;
import com.jousting.rpg.domain.model.ErkSteppeKnight;
import com.jousting.rpg.domain.model.Knight;

import java.util.List;

/**
 * Factoría para la instanciación de caballeros preconfigurados y arquetipos tácticos.
 */
public final class KnightFactory {

    private KnightFactory() {
        // Métodos estáticos de factoría
    }

    /**
     * Arquetipo equilibrado para el protagonista (controlado por jugador humano, sin IA).
     */
    public static Knight createProtagonist(String name) {
        String knightName = (name != null && !name.isBlank()) ? name : "Caballero Protagonista";
        return new Knight(
                knightName,
                100.0,
                50,
                40.0,
                EquipmentCatalog.LANCE_ROYAL,
                EquipmentCatalog.SWORD_BROAD,
                EquipmentCatalog.ARMOR_CHAINMAIL,
                EquipmentCatalog.HORSE_TOURNAMENT,
                List.of(EquipmentCatalog.HEALTH_POTION),
                null // Control humano
        );
    }

    /**
     * Arquetipo pesado y defensivo: controlado por DefensiveTankStrategy.
     */
    public static Knight createKnightOne() {
        return new Knight(
                "Sir Gideon el Muralla",
                130.0,
                40,
                55.0,
                EquipmentCatalog.MACE_WAR,
                EquipmentCatalog.AXE_BATTLE,
                EquipmentCatalog.ARMOR_PLATE,
                EquipmentCatalog.HORSE_DESTRIER,
                List.of(EquipmentCatalog.STAMINA_POTION),
                new DefensiveTankStrategy(),
                new BattleReward("GIDEON", 150, 100)
        );
    }

    /**
     * Arquetipo ligero y ágil: controlado por AgileDuelistStrategy.
     */
    public static Knight createKnightTwo() {
        return new Knight(
                "Lady Valerie la Centella",
                85.0,
                65,
                30.0,
                EquipmentCatalog.LANCE_LIGHT,
                EquipmentCatalog.SWORD_BROAD,
                EquipmentCatalog.ARMOR_CHAINMAIL,
                EquipmentCatalog.HORSE_THOROUGHBRED,
                List.of(EquipmentCatalog.HEALTH_POTION),
                new AgileDuelistStrategy(),
                new BattleReward("VALERIE", 200, 150)
        );
    }

    /**
     * Arquetipo nómada único: controlado por SteppeMasterStrategy.
     */
    public static ErkSteppeKnight createErk() {
        return new ErkSteppeKnight(
                "Erk, Caballero de las Estepas",
                105.0,
                60,
                35.0,
                EquipmentCatalog.SWORD_STEPPE,
                EquipmentCatalog.LANCE_LIGHT,
                EquipmentCatalog.ARMOR_STEPPE_LEATHER,
                EquipmentCatalog.HORSE_STEPPE_RUNNER,
                List.of(EquipmentCatalog.HEALTH_POTION, EquipmentCatalog.STAMINA_POTION),
                new SteppeMasterStrategy(),
                new BattleReward("ERK", 350, 250)
        );
    }
}
