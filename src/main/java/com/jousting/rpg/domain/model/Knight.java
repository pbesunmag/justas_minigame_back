package com.jousting.rpg.domain.model;

import com.jousting.rpg.domain.catalog.Armor;
import com.jousting.rpg.domain.catalog.ConsumableItem;
import com.jousting.rpg.domain.catalog.Horse;
import com.jousting.rpg.domain.catalog.Weapon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Estado dinámico de un caballero y su montura durante el combate.
 */
public class Knight {

    private final String name;

    // Estadísticas vitales
    private double maxHp;
    private double currentHp;

    private int maxStamina;
    private int currentStamina;

    private final double maxShieldHp;
    private double currentShieldHp;
    private boolean shieldBroken;

    private double ultimateCharge; // 0.0 a 100.0

    // Equipamiento
    private Weapon mainWeapon;
    private Weapon secondaryWeapon;
    private Weapon equippedWeapon;
    private Armor armor;
    private Horse horse;

    // Estado dinámico del caballo
    private int currentSpeed;
    private int currentFatigue;
    private boolean horsePanicked;

    // Inventario dinámico
    private final List<ConsumableItem> inventory;

    // Modificadores temporales del turno
    private boolean defending;
    private boolean dodging;

    // Estrategia de IA opcional (null para el jugador humano)
    private com.jousting.rpg.ai.CombatAIStrategy aiStrategy;

    // Progresión y economía
    private int gold;
    private int experience;
    private int level;
    private final Set<String> unlockedItemIds;
    private final Set<String> defeatedEnemies;
    private BattleReward battleReward;

    public Knight(String name, double maxHp, int maxStamina, double maxShieldHp,
                  Weapon mainWeapon, Weapon secondaryWeapon, Armor armor, Horse horse,
                  List<ConsumableItem> initialItems) {
        this(name, maxHp, maxStamina, maxShieldHp, mainWeapon, secondaryWeapon, armor, horse, initialItems, null);
    }

    public Knight(String name, double maxHp, int maxStamina, double maxShieldHp,
                  Weapon mainWeapon, Weapon secondaryWeapon, Armor armor, Horse horse,
                  List<ConsumableItem> initialItems, com.jousting.rpg.ai.CombatAIStrategy aiStrategy) {
        this(name, maxHp, maxStamina, maxShieldHp, mainWeapon, secondaryWeapon, armor, horse, initialItems, aiStrategy, null);
    }

    public Knight(String name, double maxHp, int maxStamina, double maxShieldHp,
                  Weapon mainWeapon, Weapon secondaryWeapon, Armor armor, Horse horse,
                  List<ConsumableItem> initialItems, com.jousting.rpg.ai.CombatAIStrategy aiStrategy,
                  BattleReward battleReward) {
        this.name = name;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.maxStamina = maxStamina;
        this.currentStamina = maxStamina;
        this.maxShieldHp = maxShieldHp;
        this.currentShieldHp = maxShieldHp;
        this.shieldBroken = false;
        this.ultimateCharge = 0.0;
        this.mainWeapon = mainWeapon;
        this.secondaryWeapon = secondaryWeapon;
        this.equippedWeapon = mainWeapon;
        this.armor = armor;
        this.horse = horse;
        this.currentSpeed = 1;
        this.currentFatigue = 0;
        this.horsePanicked = false;
        this.inventory = new ArrayList<>(initialItems != null ? initialItems : List.of());
        this.defending = false;
        this.dodging = false;
        this.aiStrategy = aiStrategy;

        // Inicialización de progresión
        this.gold = 0;
        this.experience = 0;
        this.level = 1;
        this.unlockedItemIds = new HashSet<>();
        if (mainWeapon != null) this.unlockedItemIds.add(mainWeapon.getId());
        if (secondaryWeapon != null) this.unlockedItemIds.add(secondaryWeapon.getId());
        if (armor != null) this.unlockedItemIds.add(armor.getId());
        if (horse != null) this.unlockedItemIds.add(horse.getId());

        this.defeatedEnemies = new HashSet<>();
        this.battleReward = battleReward;
    }

    // --- Métodos de ciclo de turno ---

    public void resetTurnState() {
        this.defending = false;
        this.dodging = false;
    }

    public boolean isAlive() {
        return this.currentHp > 0;
    }

    public double takeDamage(double rawDamage) {
        // Si el escudo está roto, se incrementa el daño un 25%
        double finalDamage = this.shieldBroken ? (rawDamage * 1.25) : rawDamage;
        this.currentHp = Math.max(0, this.currentHp - finalDamage);
        return finalDamage;
    }

    public double damageShield(double damage) {
        if (this.shieldBroken) {
            return damage; // Todo el daño pasa directamente al cuerpo
        }

        if (this.currentShieldHp > damage) {
            this.currentShieldHp -= damage;
            return 0.0;
        } else {
            double excessDamage = damage - this.currentShieldHp;
            this.currentShieldHp = 0;
            this.shieldBroken = true;
            return excessDamage;
        }
    }

    public void consumeStamina(int amount) {
        this.currentStamina = Math.max(0, this.currentStamina - amount);
    }

    public void restoreStamina(int amount) {
        this.currentStamina = Math.min(this.maxStamina, this.currentStamina + amount);
    }

    public void heal(double amount) {
        this.currentHp = Math.min(this.maxHp, this.currentHp + amount);
    }

    public void addUltimateCharge(double percentage) {
        this.ultimateCharge = Math.min(100.0, this.ultimateCharge + percentage);
    }

    public void resetUltimateCharge() {
        this.ultimateCharge = 0.0;
    }

    public boolean canUseUltimate() {
        return this.ultimateCharge >= 100.0;
    }

    public void switchWeapon() {
        if (this.equippedWeapon == this.mainWeapon && this.secondaryWeapon != null) {
            this.equippedWeapon = this.secondaryWeapon;
        } else {
            this.equippedWeapon = this.mainWeapon;
        }
    }

    public boolean removeItem(ConsumableItem item) {
        return this.inventory.remove(item);
    }

    // --- Métodos Gancho (Hooks) para cálculo delegado en el propio caballero ---

    /**
     * Calcula el multiplicador de daño según la velocidad actual del corcel.
     */
    public double calculateSpeedBonus() {
        double scaling = (this.equippedWeapon.getType() == com.jousting.rpg.domain.enums.WeaponType.LANCE) ? 0.30 : 0.15;
        return 1.0 + (this.currentSpeed * scaling);
    }

    /**
     * Modifica la ganancia de fatiga del corcel según las capacidades del jinete.
     */
    public int calculateFatigueGain(int baseGain) {
        return baseGain;
    }

    /**
     * Devuelve el valor de defensa de la armadura según la zona atacada.
     */
    public double calculateDefenseModifier(com.jousting.rpg.domain.enums.TargetZone zone) {
        return (zone == com.jousting.rpg.domain.enums.TargetZone.HEAD) 
                ? this.armor.getHeadDefense() 
                : this.armor.getChestDefense();
    }

    /**
     * Calcula la probabilidad porcentual de esquiva según velocidad y peso de armadura.
     */
    public double calculateDodgeChance() {
        double chance = 50.0 + (this.currentSpeed * 5.0) - (this.armor.getWeight() * 2.0);
        return Math.max(10.0, Math.min(85.0, chance));
    }

    /**
     * Descripción narrativa de la habilidad pasiva del personaje.
     */
    public String getPassiveDescription() {
        return "Técnica estándar de torneo";
    }

    // --- Getters & Setters para el estado dinámico ---

    public String getName() {
        return name;
    }

    public double getMaxHp() {
        return maxHp;
    }

    public double getCurrentHp() {
        return currentHp;
    }

    public int getMaxStamina() {
        return maxStamina;
    }

    public int getCurrentStamina() {
        return currentStamina;
    }

    public double getMaxShieldHp() {
        return maxShieldHp;
    }

    public double getCurrentShieldHp() {
        return currentShieldHp;
    }

    public boolean isShieldBroken() {
        return shieldBroken;
    }

    public double getUltimateCharge() {
        return ultimateCharge;
    }

    public Weapon getMainWeapon() {
        return mainWeapon;
    }

    public Weapon getSecondaryWeapon() {
        return secondaryWeapon;
    }

    public Weapon getEquippedWeapon() {
        return equippedWeapon;
    }

    public Armor getArmor() {
        return armor;
    }

    public Horse getHorse() {
        return horse;
    }

    public int getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(int currentSpeed) {
        this.currentSpeed = Math.max(1, Math.min(this.horse.getMaxSpeed(), currentSpeed));
    }

    public int getCurrentFatigue() {
        return currentFatigue;
    }

    public void setCurrentFatigue(int currentFatigue) {
        this.currentFatigue = Math.max(0, Math.min(this.horse.getMaxFatigue(), currentFatigue));
    }

    public boolean isHorsePanicked() {
        return horsePanicked;
    }

    public void setHorsePanicked(boolean horsePanicked) {
        this.horsePanicked = horsePanicked;
    }

    public List<ConsumableItem> getInventory() {
        return inventory;
    }

    public boolean isDefending() {
        return defending;
    }

    public void setDefending(boolean defending) {
        this.defending = defending;
    }

    public boolean isDodging() {
        return dodging;
    }

    public void setDodging(boolean dodging) {
        this.dodging = dodging;
    }

    public com.jousting.rpg.ai.CombatAIStrategy getAiStrategy() {
        return aiStrategy;
    }

    public void setAiStrategy(com.jousting.rpg.ai.CombatAIStrategy aiStrategy) {
        this.aiStrategy = aiStrategy;
    }

    public boolean isAiControlled() {
        return this.aiStrategy != null;
    }

    public com.jousting.rpg.ai.TurnAction decideTurn(Knight opponent, BattleState state) {
        if (this.aiStrategy != null) {
            return this.aiStrategy.decideTurn(this, opponent, state);
        }
        return null;
    }

    // --- Métodos de Progresión y Armería ---

    public int getGold() {
        return gold;
    }

    public void addGold(int amount) {
        this.gold += Math.max(0, amount);
    }

    public boolean spendGold(int amount) {
        if (amount > 0 && this.gold >= amount) {
            this.gold -= amount;
            return true;
        }
        return false;
    }

    public int getExperience() {
        return experience;
    }

    public void addExperience(int amount) {
        this.experience += Math.max(0, amount);
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getLevel() {
        return level;
    }

    public void levelUp() {
        this.level++;
        this.maxHp += 15.0;
        this.maxStamina += 10;
        this.currentHp = this.maxHp;
        this.currentStamina = this.maxStamina;
    }

    public Set<String> getUnlockedItemIds() {
        return unlockedItemIds;
    }

    public void unlockItem(String itemId) {
        if (itemId != null) {
            this.unlockedItemIds.add(itemId);
        }
    }

    public boolean hasItemUnlocked(String itemId) {
        return itemId != null && this.unlockedItemIds.contains(itemId);
    }

    public Set<String> getDefeatedEnemies() {
        return defeatedEnemies;
    }

    public void recordDefeatedEnemy(String enemyCode) {
        if (enemyCode != null) {
            this.defeatedEnemies.add(enemyCode);
        }
    }

    public boolean isEnemyDefeated(String enemyCode) {
        return enemyCode != null && this.defeatedEnemies.contains(enemyCode);
    }

    public BattleReward getBattleReward() {
        return battleReward;
    }

    public void setBattleReward(BattleReward battleReward) {
        this.battleReward = battleReward;
    }

    public void setMainWeapon(Weapon mainWeapon) {
        this.mainWeapon = mainWeapon;
        if (this.equippedWeapon == null || this.equippedWeapon == this.mainWeapon) {
            this.equippedWeapon = mainWeapon;
        }
    }

    public void setSecondaryWeapon(Weapon secondaryWeapon) {
        this.secondaryWeapon = secondaryWeapon;
    }

    public void setEquippedWeapon(Weapon weapon) {
        this.equippedWeapon = weapon;
    }

    public void setArmor(Armor armor) {
        this.armor = armor;
    }

    public void setHorse(Horse horse) {
        this.horse = horse;
    }
}
