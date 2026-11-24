package com.jousting.rpg.domain.catalog;

import com.jousting.rpg.domain.enums.ArmorAffinity;
import com.jousting.rpg.domain.enums.ConsumableType;
import com.jousting.rpg.domain.enums.HorseTemperament;
import com.jousting.rpg.domain.enums.WeaponType;

import java.util.List;
import java.util.Optional;

/**
 * Catálogo estático centralizado de equipamiento inicial y desbloqueable en armería.
 */
public final class EquipmentCatalog {

    private EquipmentCatalog() {
        // Clase de constantes de catálogo
    }

    // --- Armas básicas iniciales (Triángulo táctico) ---
    public static final Weapon BASIC_LANCE = new Weapon(
            "BASIC_LANCE", "Lanza de Madera Reforzada", WeaponType.LANCE, 24.0, 1.1, 12, ArmorAffinity.MEDIUM,
            0, 1, null);

    public static final Weapon BASIC_SWORD = new Weapon(
            "BASIC_SWORD", "Espada de Escudero", WeaponType.SWORD, 19.0, 0.9, 9, ArmorAffinity.LIGHT,
            0, 1, null);

    public static final Weapon BASIC_MACE = new Weapon(
            "BASIC_MACE", "Maza de Hierro Básica", WeaponType.MACE, 21.0, 1.8, 14, ArmorAffinity.HEAVY,
            0, 1, null);

    // --- Armas ---
    public static final Weapon LANCE_ROYAL = new Weapon(
            "wpn_lance_royal", "Lanza de Justa Real", WeaponType.LANCE, 28.0, 1.2, 15, ArmorAffinity.MEDIUM,
            0, 1, null);

    public static final Weapon LANCE_LIGHT = new Weapon(
            "wpn_lance_light", "Lanza Ligera de Caballería", WeaponType.LANCE, 24.0, 1.0, 10, ArmorAffinity.LIGHT,
            90, 2, null);

    public static final Weapon MACE_WAR = new Weapon(
            "wpn_mace_war", "Maza de Guerra Pesada", WeaponType.MACE, 26.0, 2.0, 18, ArmorAffinity.HEAVY,
            120, 2, "GIDEON");

    public static final Weapon SWORD_BROAD = new Weapon(
            "wpn_sword_broad", "Espada Ancha de Acero", WeaponType.SWORD, 20.0, 0.9, 10, ArmorAffinity.LIGHT,
            0, 1, null);

    public static final Weapon SWORD_STEPPE = new Weapon(
            "wpn_sword_steppe", "Sable Curvo de las Estepas", WeaponType.SWORD, 22.0, 1.1, 12, ArmorAffinity.LIGHT,
            220, 3, "ERK");

    public static final Weapon AXE_BATTLE = new Weapon(
            "wpn_axe_battle", "Hacha de Batalla", WeaponType.AXE, 32.0, 1.5, 25, ArmorAffinity.MEDIUM,
            180, 2, null);

    // --- Armaduras ---
    public static final Armor ARMOR_PLATE = new Armor(
            "arm_plate", "Placas Completas de Torneo", ArmorAffinity.HEAVY, 22, 25.0, 40.0,
            250, 2, "GIDEON");

    public static final Armor ARMOR_CHAINMAIL = new Armor(
            "arm_chain", "Cota de Malla Reforzada", ArmorAffinity.MEDIUM, 12, 15.0, 25.0,
            0, 1, null);

    public static final Armor ARMOR_STEPPE_LEATHER = new Armor(
            "arm_steppe", "Cuero Endurecido y Pieles", ArmorAffinity.LIGHT, 6, 10.0, 18.0,
            160, 2, "VALERIE");

    // --- Caballos ---
    public static final Horse HORSE_TOURNAMENT = new Horse(
            "hrs_tourney", "Corcel de Torneo Imperial", 4, 8, HorseTemperament.CALM,
            0, 1, null);

    public static final Horse HORSE_DESTRIER = new Horse(
            "hrs_destrier", "Titán Percherón", 3, 10, HorseTemperament.STEADFAST,
            200, 2, "GIDEON");

    public static final Horse HORSE_THOROUGHBRED = new Horse(
            "hrs_pure", "Centella Ágil", 5, 7, HorseTemperament.AGGRESSIVE,
            220, 2, "VALERIE");

    public static final Horse HORSE_STEPPE_RUNNER = new Horse(
            "hrs_steppe", "Viento del Desierto", 4, 9, HorseTemperament.STEADFAST,
            300, 3, "ERK");

    // --- Consumibles ---
    public static final ConsumableItem HEALTH_POTION = new ConsumableItem(
            "pot_health", "Poción de Salud", ConsumableType.HEALTH_POTION, 30);

    public static final ConsumableItem STAMINA_POTION = new ConsumableItem(
            "pot_stamina", "Elixir de Vigor", ConsumableType.STAMINA_POTION, 25);

    private static final List<Equipment> ALL_EQUIPMENT = List.of(
            BASIC_LANCE, BASIC_SWORD, BASIC_MACE,
            LANCE_ROYAL, LANCE_LIGHT, MACE_WAR, SWORD_BROAD, SWORD_STEPPE, AXE_BATTLE,
            ARMOR_PLATE, ARMOR_CHAINMAIL, ARMOR_STEPPE_LEATHER,
            HORSE_TOURNAMENT, HORSE_DESTRIER, HORSE_THOROUGHBRED, HORSE_STEPPE_RUNNER
    );

    public static List<Equipment> getAllEquipment() {
        return ALL_EQUIPMENT;
    }

    public static Optional<Equipment> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return ALL_EQUIPMENT.stream()
                .filter(eq -> eq.getId().equalsIgnoreCase(id))
                .findFirst();
    }
}
