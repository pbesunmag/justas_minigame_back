package com.jousting.rpg.service;

import com.jousting.rpg.ai.TurnAction;
import com.jousting.rpg.domain.enums.HorseOrder;
import com.jousting.rpg.domain.enums.TargetZone;
import com.jousting.rpg.domain.factory.KnightFactory;
import com.jousting.rpg.domain.model.BattleState;
import com.jousting.rpg.domain.model.ErkSteppeKnight;
import com.jousting.rpg.domain.model.Knight;
import com.jousting.rpg.dto.PlayerTurnChoice;
import com.jousting.rpg.dto.TurnResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatEngineTest {

    private CombatEngineService combatEngine;

    @BeforeEach
    void setUp() {
        // Semilla fija para reproducibilidad en pruebas
        Random fixedRandom = new Random(42);
        combatEngine = new CombatEngineService(fixedRandom);
    }

    @Test
    @DisplayName("Simular justa completa usando KnightFactory hasta victoria o escudo roto")
    void shouldSimulateFullJoustMatch() {
        System.out.println("\n=======================================================");
        System.out.println("   GRAN TORNEO: PROTAGONISTA VS CABALLERO PESADO");
        System.out.println("=======================================================");

        Knight protagonist = KnightFactory.createProtagonist("Sir Lancelot");
        Knight heavyKnight = KnightFactory.createKnightOne();
        BattleState battleState = new BattleState(protagonist, heavyKnight);

        boolean shieldBrokenObserved = false;
        int maxRounds = 8;

        for (int round = 1; round <= maxRounds && !battleState.isBattleOver(); round++) {
            PlayerTurnChoice choice1;
            PlayerTurnChoice choice2;

            if (round == 1) {
                choice1 = PlayerTurnChoice.attack(TargetZone.CHEST, HorseOrder.ACCELERATE);
                choice2 = PlayerTurnChoice.attack(TargetZone.SHIELD, HorseOrder.ACCELERATE);
            } else if (round == 2) {
                choice1 = PlayerTurnChoice.attack(TargetZone.HEAD, HorseOrder.ACCELERATE);
                choice2 = PlayerTurnChoice.attack(TargetZone.SHIELD, HorseOrder.MAINTAIN);
            } else if (round == 3) {
                choice1 = PlayerTurnChoice.dodge(HorseOrder.MAINTAIN);
                choice2 = PlayerTurnChoice.attack(TargetZone.CHEST, HorseOrder.ACCELERATE);
            } else if (round == 4) {
                choice1 = PlayerTurnChoice.attack(TargetZone.CHEST, HorseOrder.MAINTAIN);
                choice2 = PlayerTurnChoice.defend(HorseOrder.DECELERATE);
            } else {
                choice1 = protagonist.canUseUltimate()
                        ? PlayerTurnChoice.ultimate(TargetZone.CHEST, HorseOrder.MAINTAIN)
                        : PlayerTurnChoice.attack(TargetZone.CHEST, HorseOrder.ACCELERATE);

                choice2 = heavyKnight.canUseUltimate()
                        ? PlayerTurnChoice.ultimate(TargetZone.HEAD, HorseOrder.MAINTAIN)
                        : PlayerTurnChoice.attack(TargetZone.SHIELD, HorseOrder.PET);
            }

            TurnResult result = combatEngine.resolveTurn(battleState, choice1, choice2);

            for (String log : result.getLogs()) {
                System.out.println(log);
            }

            System.out.printf("-> Post-turno %d: [%s: HP=%.1f/%.1f, Escudo=%.1f/%.1f, Stam=%.0f%%] vs [%s: HP=%.1f/%.1f, Escudo=%.1f/%.1f, Stam=%.0f%%]%n%n",
                    result.getTurnNumber(),
                    protagonist.getName(), protagonist.getCurrentHp(), protagonist.getMaxHp(),
                    protagonist.getCurrentShieldHp(), protagonist.getMaxShieldHp(),
                    (double) protagonist.getCurrentStamina(),
                    heavyKnight.getName(), heavyKnight.getCurrentHp(), heavyKnight.getMaxHp(),
                    heavyKnight.getCurrentShieldHp(), heavyKnight.getMaxShieldHp(),
                    (double) heavyKnight.getCurrentStamina()
            );

            if (protagonist.isShieldBroken() || heavyKnight.isShieldBroken()) {
                shieldBrokenObserved = true;
            }
        }

        assertNotNull(battleState);
        assertTrue(battleState.isBattleOver() || shieldBrokenObserved);
    }

    @Test
    @DisplayName("Comprobar pasivas únicas de Erk, Caballero de las Estepas")
    void shouldVerifyErkSteppeKnightPassive() {
        System.out.println("\n=======================================================");
        System.out.println("   DEMOSTRACIÓN DE HABILIDADES: ERK DE LAS ESTEPAS");
        System.out.println("=======================================================");

        ErkSteppeKnight erk = KnightFactory.createErk();
        Knight standardKnight = KnightFactory.createKnightTwo();

        erk.setCurrentSpeed(3);
        standardKnight.setCurrentSpeed(3);

        double erkSpeedBonus = erk.calculateSpeedBonus();
        standardKnight.switchWeapon();
        double standardSpeedBonus = standardKnight.calculateSpeedBonus();

        assertEquals(2.05, erkSpeedBonus, 0.001);
        assertTrue(erkSpeedBonus > standardSpeedBonus);

        int erkFatigue = erk.calculateFatigueGain(2);
        int standardFatigue = standardKnight.calculateFatigueGain(2);

        assertEquals(1, erkFatigue);
        assertEquals(2, standardFatigue);
    }

    @Test
    @DisplayName("Simular combate PvE: Protagonista vs Erk gobernado por SteppeMasterStrategy")
    void shouldSimulatePvEMatchAgainstErkAI() {
        System.out.println("\n=======================================================");
        System.out.println("   DUELO PVE: PROTAGONISTA VS ERK (AI STEPPE MASTER)");
        System.out.println("=======================================================\n");

        Knight player = KnightFactory.createProtagonist("Rodrigo el Desafiante");
        ErkSteppeKnight erk = KnightFactory.createErk();

        assertNull(player.getAiStrategy(), "El jugador humano no debe tener estrategia IA.");
        assertNotNull(erk.getAiStrategy(), "Erk debe tener asignada su estrategia de IA.");
        assertTrue(erk.isAiControlled(), "Erk debe estar marcado como controlado por IA.");

        BattleState battleState = new BattleState(player, erk);
        int maxRounds = 10;

        for (int round = 1; round <= maxRounds && !battleState.isBattleOver(); round++) {
            // Decisión táctica del jugador humano
            PlayerTurnChoice playerChoice;
            if (round == 1) {
                playerChoice = PlayerTurnChoice.attack(TargetZone.CHEST, HorseOrder.ACCELERATE);
            } else if (round == 2) {
                playerChoice = PlayerTurnChoice.attack(TargetZone.SHIELD, HorseOrder.ACCELERATE);
            } else if (round == 3) {
                playerChoice = PlayerTurnChoice.dodge(HorseOrder.MAINTAIN);
            } else if (player.canUseUltimate()) {
                playerChoice = PlayerTurnChoice.ultimate(TargetZone.CHEST, HorseOrder.MAINTAIN);
            } else {
                playerChoice = PlayerTurnChoice.attack(TargetZone.CHEST, HorseOrder.MAINTAIN);
            }

            // Decisión autónoma de la IA de Erk
            TurnAction erkChoice = erk.decideTurn(player, battleState);
            assertNotNull(erkChoice, "La IA de Erk siempre debe formular una decisión táctica.");

            // Resolución del turno en el motor de combate
            TurnResult result = combatEngine.resolveTurn(battleState, playerChoice, erkChoice);

            for (String log : result.getLogs()) {
                System.out.println(log);
            }

            System.out.printf("-> Fin Pase #%d | [%s: HP=%.1f/%.1f, Esc=%.1f/%.1f, Vel=%d, Fatiga=%d] vs [%s: HP=%.1f/%.1f, Esc=%.1f/%.1f, Vel=%d, Fatiga=%d]%n%n",
                    result.getTurnNumber(),
                    player.getName(), player.getCurrentHp(), player.getMaxHp(), player.getCurrentShieldHp(), player.getMaxShieldHp(), player.getCurrentSpeed(), player.getCurrentFatigue(),
                    erk.getName(), erk.getCurrentHp(), erk.getMaxHp(), erk.getCurrentShieldHp(), erk.getMaxShieldHp(), erk.getCurrentSpeed(), erk.getCurrentFatigue()
            );
        }

        assertTrue(battleState.getTurnNumber() > 0, "Al menos debe resolverse un turno.");
        assertTrue(battleState.isBattleOver() || player.isShieldBroken() || erk.isShieldBroken(),
                "El combate debe haber concluido o un escudo haberse roto.");
    }

    public static void main(String[] args) {
        CombatEngineTest test = new CombatEngineTest();
        test.setUp();
        test.shouldSimulateFullJoustMatch();
        test.shouldVerifyErkSteppeKnightPassive();
        test.shouldSimulatePvEMatchAgainstErkAI();
    }
}
