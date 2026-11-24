package com.jousting.rpg.model.entity;

import com.jousting.rpg.model.enums.TipoCaballero;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "caballeros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Caballero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoCaballero tipo;

    @Column(name = "estrategia_ia")
    private String estrategiaIa;

    @Column(name = "hp_base_cabeza", nullable = false)
    private Integer hpBaseCabeza;

    @Column(name = "hp_base_torso", nullable = false)
    private Integer hpBaseTorso;

    @Column(name = "fuerza", nullable = false)
    private Integer fuerza;

    @Column(name = "destreza", nullable = false)
    private Integer destreza;

    @ManyToOne
    @JoinColumn(name = "arma_id")
    private Arma arma;

    @ManyToOne
    @JoinColumn(name = "yelmo_id")
    private Armadura yelmo;

    @ManyToOne
    @JoinColumn(name = "peto_id")
    private Armadura peto;

    @ManyToOne
    @JoinColumn(name = "escudo_id")
    private Escudo escudo;

    @ManyToOne
    @JoinColumn(name = "caballo_id")
    private Caballo caballo;
}
