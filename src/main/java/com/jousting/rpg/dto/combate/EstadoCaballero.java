package com.jousting.rpg.dto.combate;

import com.jousting.rpg.model.entity.Arma;
import com.jousting.rpg.model.entity.Armadura;
import com.jousting.rpg.model.entity.Caballero;
import com.jousting.rpg.model.entity.Caballo;
import com.jousting.rpg.model.entity.Escudo;
import com.jousting.rpg.model.enums.TipoCaballero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoCaballero {

    private Long caballeroId;
    private String nombre;
    private TipoCaballero tipo;
    private String estrategiaIa;

    // Salud por zonas
    private int hpCabezaMax;
    private int hpCabezaActual;
    private int hpTorsoMax;
    private int hpTorsoActual;

    // Escudo
    private int durabilidadEscudoMax;
    private int durabilidadEscudoActual;
    private int absorcionDanoEscudo;
    private boolean escudoRoto;

    // Montura y stamina
    private int staminaMax;
    private int staminaActual;
    private int velocidadMax;
    private int consumoStamina;

    // Peso y atributos de combate
    private int pesoTotal;
    private int fuerza;
    private int destreza;
    private int danoBaseArma;
    private int precisionBaseArma;
    private int defensaCabeza;
    private int defensaTorso;

    public static EstadoCaballero desdeEntidad(Caballero caballero) {
        Arma arma = caballero.getArma();
        Armadura yelmo = caballero.getYelmo();
        Armadura peto = caballero.getPeto();
        Escudo escudo = caballero.getEscudo();
        Caballo caballo = caballero.getCaballo();

        int hpCabezaMax = valorOZero(caballero.getHpBaseCabeza()) + (yelmo != null ? valorOZero(yelmo.getHpExtra()) : 0);
        int hpTorsoMax = valorOZero(caballero.getHpBaseTorso()) + (peto != null ? valorOZero(peto.getHpExtra()) : 0);

        boolean tieneEscudo = escudo != null && valorOZero(escudo.getDurabilidadMax()) > 0;
        int durabilidadEscudo = tieneEscudo ? escudo.getDurabilidadMax() : 0;
        int absorcionEscudo = tieneEscudo ? valorOZero(escudo.getAbsorcionDano()) : 0;

        int staminaMax = caballo != null ? valorOZero(caballo.getResistenciaMax()) : 0;
        int velocidadMax = caballo != null ? valorOZero(caballo.getVelocidadMax()) : 0;
        int consumoStamina = caballo != null ? valorOZero(caballo.getConsumoStamina()) : 0;

        int pesoTotal = (arma != null ? valorOZero(arma.getPeso()) : 0)
                + (yelmo != null ? valorOZero(yelmo.getPeso()) : 0)
                + (peto != null ? valorOZero(peto.getPeso()) : 0)
                + (escudo != null ? valorOZero(escudo.getPeso()) : 0);

        return EstadoCaballero.builder()
                .caballeroId(caballero.getId())
                .nombre(caballero.getNombre())
                .tipo(caballero.getTipo())
                .estrategiaIa(caballero.getEstrategiaIa())
                .hpCabezaMax(hpCabezaMax)
                .hpCabezaActual(hpCabezaMax)
                .hpTorsoMax(hpTorsoMax)
                .hpTorsoActual(hpTorsoMax)
                .durabilidadEscudoMax(durabilidadEscudo)
                .durabilidadEscudoActual(durabilidadEscudo)
                .absorcionDanoEscudo(absorcionEscudo)
                .escudoRoto(!tieneEscudo)
                .staminaMax(staminaMax)
                .staminaActual(staminaMax)
                .velocidadMax(velocidadMax)
                .consumoStamina(consumoStamina)
                .pesoTotal(pesoTotal)
                .fuerza(valorOZero(caballero.getFuerza()))
                .destreza(valorOZero(caballero.getDestreza()))
                .danoBaseArma(arma != null ? valorOZero(arma.getDanoBase()) : 10)
                .precisionBaseArma(arma != null ? valorOZero(arma.getPrecisionBase()) : 60)
                .defensaCabeza(yelmo != null ? valorOZero(yelmo.getDefensa()) : 0)
                .defensaTorso(peto != null ? valorOZero(peto.getDefensa()) : 0)
                .build();
    }

    public boolean estaDerrotado() {
        return hpCabezaActual <= 0 || hpTorsoActual <= 0;
    }

    public double getPorcentajeHpTotal() {
        int maxTotal = hpCabezaMax + hpTorsoMax;
        if (maxTotal <= 0) {
            return 0.0;
        }
        return (double) (hpCabezaActual + hpTorsoActual) / maxTotal;
    }

    private static int valorOZero(Integer valor) {
        return valor != null ? valor : 0;
    }
}
