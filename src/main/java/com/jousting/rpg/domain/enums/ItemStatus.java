package com.jousting.rpg.domain.enums;

/**
 * Estado comercial y de posesión de un objeto en la armería.
 */
public enum ItemStatus {
    LOCKED,      // Bloqueado por nivel o jefe no derrotado
    AVAILABLE,   // Desbloqueado y disponible para compra con oro
    OWNED,       // Ya adquirido en el inventario del caballero
    EQUIPPED     // Actualmente equipado en combate
}
