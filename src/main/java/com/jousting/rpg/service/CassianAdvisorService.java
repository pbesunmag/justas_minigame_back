package com.jousting.rpg.service;

import com.jousting.rpg.domain.enums.WeaponType;
import com.jousting.rpg.domain.model.BattleState;
import com.jousting.rpg.domain.model.Knight;
import org.springframework.stereotype.Service;

/**
 * Servicio táctico de Cassian, el viejo y entusiasta escudero que aconseja al jugador en la arena.
 */
@Service
public class CassianAdvisorService {

    /**
     * Genera un consejo táctico corto en español evaluando el estado actual de la justa.
     */
    public String generateTip(BattleState battleState, Knight player, Knight enemy) {
        if (player == null || enemy == null) {
            return "¡Sujeta bien esas riendas, mi señor! La liza espera tu valor.";
        }

        if (battleState != null && battleState.isBattleOver()) {
            if (player.getName().equalsIgnoreCase(battleState.getWinnerName())) {
                return "¡Por los viejos reyes, qué victoria tan gloriosa! ¡Hoy brindaremos en la taberna!";
            }
            return "¡Arriba ese ánimo, mi señor! Hasta los grandes campeones muerden el polvo antes de triunfar.";
        }

        // 1. Salud crítica del jugador
        double playerHpRatio = player.getMaxHp() > 0 ? player.getCurrentHp() / player.getMaxHp() : 1.0;
        if (playerHpRatio <= 0.30) {
            if (player.getInventory() != null && !player.getInventory().isEmpty()) {
                return "¡Cuidado, mi señor, sangras demasiado! ¡Bebe una poción o alza el escudo antes de la próxima carga!";
            }
            return "¡Tu armadura apenas aguanta! ¡Defiéndete o esquiva este pase para no caer de la silla!";
        }

        // 2. Habilidad definitiva cargada
        if (player.canUseUltimate()) {
            return "¡El público ruge y tu ímpetu está al máximo! ¡Desata tu golpe definitivo ahora mismo!";
        }

        // 3. Rival a máxima velocidad
        if (enemy.getHorse() != null && enemy.getCurrentSpeed() >= enemy.getHorse().getMaxSpeed()) {
            return "¡Ojo, mi señor! ¡" + enemy.getName() + " galopa a máxima velocidad! ¡Esquiva o cubre bien con el escudo!";
        }

        // 4. Escudo enemigo roto o a punto de romperse
        double enemyShieldRatio = enemy.getMaxShieldHp() > 0 ? enemy.getCurrentShieldHp() / enemy.getMaxShieldHp() : 0.0;
        if (enemy.isShieldBroken()) {
            return "¡Su escudo es astillas! ¡Acelera con la lanza o remata con la espada para hacerle daño brutal!";
        }
        if (enemyShieldRatio <= 0.30) {
            return "¡Ese escudo suyo cruje por todas partes! ¡Un buen golpe de maza lo hará volar en pedazos!";
        }

        // 5. Caballo enemigo fatigado o en pánico
        if (enemy.isHorsePanicked() || (enemy.getHorse() != null && enemy.getCurrentFatigue() >= enemy.getHorse().getMaxFatigue() - 2)) {
            return "¡Mira cómo resopla su corcel! ¡Está agotado, aprovecha para acelerar y golpear sin piedad!";
        }

        // 6. Caballo del jugador fatigado o en pánico
        if (player.isHorsePanicked() || (player.getHorse() != null && player.getCurrentFatigue() >= player.getHorse().getMaxFatigue() - 2)) {
            return "¡Pobre bestia, necesita aire! ¡Frena o dale un respiro a tu caballo antes de que entre en pánico!";
        }

        // 7. Stamina baja del jugador
        if (player.getCurrentStamina() <= 15) {
            return "¡Te falta el aliento, mi señor! ¡Defiéndete o usa la espada ligera para recuperar vigor!";
        }

        // 8. Consejo del triángulo de armas (Espada / Lanza / Maza)
        WeaponType playerWeaponType = player.getEquippedWeapon() != null ? player.getEquippedWeapon().getType() : null;
        if (enemyShieldRatio > 0.65 && playerWeaponType != WeaponType.MACE) {
            return "¡Su escudo está intacto y recio! ¡Cambia a la maza si quieres aplastarle la defensa!";
        }
        if (player.getCurrentSpeed() >= 3 && playerWeaponType != WeaponType.LANCE) {
            return "¡Menudo galope llevamos! ¡Con la lanza en ristre aprovecharás toda esa velocidad!";
        }
        if (enemy.getCurrentSpeed() >= 3 && playerWeaponType != WeaponType.SWORD) {
            return "¡Ese rival es rápido como un rayo! ¡La espada te dará golpes ágiles sin agotar tu brazo!";
        }

        // 9. Turno de apertura o ánimo general
        if (battleState != null && battleState.getTurnNumber() == 0) {
            return "¡Suenan las trompetas, mi señor! ¡Toma impulso con tu corcel y mide bien la guardia de " + enemy.getName() + "!";
        }

        return "¡Bien jugado, mi señor! ¡Alterna entre lanza, maza y espada según su guardia y la victoria será nuestra!";
    }
}
