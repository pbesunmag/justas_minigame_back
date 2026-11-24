package com.jousting.rpg.domain.enums;

/**
 * Temperamento del caballo que modula su ganancia y pérdida de fatiga/nerviosismo.
 */
public enum HorseTemperament {
    CALM,       // Gana menos fatiga al acelerar
    AGGRESSIVE, // Mayor aceleración pero se fatiga antes
    NERVOUS,    // Reacciona muy bien a las caricias (PET)
    STEADFAST   // Resistente al pánico
}
