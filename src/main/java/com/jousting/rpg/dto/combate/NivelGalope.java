package com.jousting.rpg.dto.combate;

public enum NivelGalope {
    TROTE(0.7),
    GALOPE(1.0),
    CARGA_MAXIMA(1.35);

    private final double multiplicadorVelocidad;

    NivelGalope(double multiplicadorVelocidad) {
        this.multiplicadorVelocidad = multiplicadorVelocidad;
    }

    public double getMultiplicadorVelocidad() {
        return multiplicadorVelocidad;
    }
}
