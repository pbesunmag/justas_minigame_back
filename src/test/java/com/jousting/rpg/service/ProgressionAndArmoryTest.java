package com.jousting.rpg.service;

import com.jousting.rpg.ai.TurnAction;
import com.jousting.rpg.domain.enums.HorseOrder;
import com.jousting.rpg.domain.enums.ItemStatus;
import com.jousting.rpg.domain.enums.TargetZone;
import com.jousting.rpg.domain.factory.KnightFactory;
import com.jousting.rpg.domain.model.BattleState;
import com.jousting.rpg.domain.model.ErkSteppeKnight;
import com.jousting.rpg.domain.model.Knight;
import com.jousting.rpg.dto.ArmoryItemDto;
import com.jousting.rpg.dto.PlayerTurnChoice;
import com.jousting.rpg.dto.RewardSummary;
import com.jousting.rpg.dto.TurnResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressionAndArmoryTest {

    private ProgressionService progressionService;
    private ArmoryService armoryService;
    private CombatEngineService combatEngine;

    @BeforeEach
    void setUp() {
        progressionService = new ProgressionService();
        armoryService = new ArmoryService();
        combatEngine = new CombatEngineService(new Random(42));
    }

    @Test
    @DisplayName("Flujo completo: victoria, recompensa, subida de nivel, compra en armería y duelo con Erk")
    void shouldCompleteFullProgressionAndArmoryFlow() {
        System.out.println("\n=======================================================");
        System.out.println("   SIMULACIÓN DE PROGRESIÓN, ARMERÍA Y COMBATE RPG");
        System.out.println("=======================================================\n");

        // 1. Crear al protagonista en Nivel 1
        Knight player = KnightFactory.createProtagonist("Rodrigo de Vivar");
        assertEquals(1, player.getLevel());
        assertEquals(0, player.getGold());
        assertEquals(0, player.getExperience());
        assertEquals(100.0, player.getMaxHp());
        assertEquals(50, player.getMaxStamina());

        // 2. Verificar catálogo inicial en la armería
        List<ArmoryItemDto> initialCatalog = armoryService.getCatalog(player);
        Optional<ArmoryItemDto> maceBefore = initialCatalog.stream()
                .filter(item -> item.getItemId().equals("wpn_mace_war"))
                .findFirst();

        assertTrue(maceBefore.isPresent());
        assertEquals(ItemStatus.LOCKED, maceBefore.get().getStatus(), "La maza debe estar bloqueada al inicio.");
        System.out.printf("[Armería Inicial] %s: Estado=%s | Motivo: %s%n",
                maceBefore.get().getName(), maceBefore.get().getStatus(), maceBefore.get().getLockReason());

        // 3. Simular victoria contra Sir Gideon (Caballero Pesado)
        Knight gideon = KnightFactory.createKnightOne();
        System.out.println("\n--- 1. Derrota de Sir Gideon el Muralla ---");
        RewardSummary reward = progressionService.grantVictoryRewards(player, gideon);

        for (String log : reward.getLogs()) {
            System.out.println(log);
        }

        // Validar recompensas de primera victoria
        assertTrue(reward.isFirstVictory());
        assertEquals(150, reward.getGoldEarned());
        assertEquals(100, reward.getXpEarned());
        assertTrue(reward.isLeveledUp());
        assertEquals(2, player.getLevel());
        assertEquals(150, player.getGold());
        assertEquals(115.0, player.getMaxHp(), "La salud máxima debe aumentar +15.");
        assertEquals(60, player.getMaxStamina(), "La resistencia máxima debe aumentar +10.");
        assertTrue(player.isEnemyDefeated("GIDEON"), "Gideon debe constar como derrotado.");

        // 4. Comprobar que la Maza de Guerra ahora está disponible
        List<ArmoryItemDto> catalogPostVictory = armoryService.getCatalog(player);
        Optional<ArmoryItemDto> maceAfter = catalogPostVictory.stream()
                .filter(item -> item.getItemId().equals("wpn_mace_war"))
                .findFirst();

        assertTrue(maceAfter.isPresent());
        assertEquals(ItemStatus.AVAILABLE, maceAfter.get().getStatus(), "La maza debe estar disponible para compra.");
        System.out.printf("%n[Armería Post-Victoria] %s: Estado=%s (Precio: %d oro)%n",
                maceAfter.get().getName(), maceAfter.get().getStatus(), maceAfter.get().getPrice());

        // 5. Comprar la Maza de Guerra en la armería
        System.out.println("\n--- 2. Compra y Equipación en la Armería ---");
        boolean purchaseSuccess = armoryService.buyEquipment(player, "wpn_mace_war");
        assertTrue(purchaseSuccess, "La compra de la maza debe ser exitosa.");
        assertEquals(30, player.getGold(), "El oro restante debe ser 30 (150 - 120).");
        assertTrue(player.hasItemUnlocked("wpn_mace_war"));

        // Intentar comprar de nuevo debe fallar
        assertFalse(armoryService.buyEquipment(player, "wpn_mace_war"), "No se debe poder recomprar un objeto ya adquirido.");

        // 6. Equipar la Maza de Guerra
        boolean equipSuccess = armoryService.equipItem(player, "wpn_mace_war");
        assertTrue(equipSuccess, "Debe equiparse correctamente.");
        assertEquals("wpn_mace_war", player.getEquippedWeapon().getId());
        System.out.printf("¡%s ha equipado con éxito su nueva arma: %s! (Multiplicador a escudo: %.1fx)%n",
                player.getName(), player.getEquippedWeapon().getName(), player.getEquippedWeapon().getShieldDamageMultiplier());

        // Verificar que en la armería ahora figura como EQUIPPED
        List<ArmoryItemDto> catalogPostEquip = armoryService.getCatalog(player);
        Optional<ArmoryItemDto> maceEquipped = catalogPostEquip.stream()
                .filter(item -> item.getItemId().equals("wpn_mace_war"))
                .findFirst();
        assertTrue(maceEquipped.isPresent());
        assertEquals(ItemStatus.EQUIPPED, maceEquipped.get().getStatus());

        // 7. Simular combate contra Erk usando la nueva Maza de Guerra
        System.out.println("\n--- 3. Entrada a la Arena con el Nuevo Equipo: Duelo contra Erk ---");
        ErkSteppeKnight erk = KnightFactory.createErk();
        BattleState battleState = new BattleState(player, erk);

        // Turno 1: Ambos aceleran, Rodrigo apunta al escudo de Erk con la maza (2.0x daño a escudo)
        PlayerTurnChoice playerChoice = PlayerTurnChoice.attack(TargetZone.SHIELD, HorseOrder.ACCELERATE);
        TurnAction erkChoice = erk.decideTurn(player, battleState);

        TurnResult turnResult = combatEngine.resolveTurn(battleState, playerChoice, erkChoice);

        for (String log : turnResult.getLogs()) {
            System.out.println(log);
        }

        System.out.printf("%n-> Post-Turno 1: %s (HP=%.1f, Escudo=%.1f) vs %s (HP=%.1f, Escudo=%.1f)%n",
                player.getName(), player.getCurrentHp(), player.getCurrentShieldHp(),
                erk.getName(), erk.getCurrentHp(), erk.getCurrentShieldHp());

        assertNotNull(turnResult);
        assertTrue(erk.isShieldBroken(), "La maza pesada debe quebrar el escudo de Erk en el primer pase directo.");
    }

    public static void main(String[] args) {
        ProgressionAndArmoryTest test = new ProgressionAndArmoryTest();
        test.setUp();
        test.shouldCompleteFullProgressionAndArmoryFlow();
    }
}
