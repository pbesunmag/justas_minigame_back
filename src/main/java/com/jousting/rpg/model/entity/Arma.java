package com.jousting.rpg.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "armas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Arma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "dano_base", nullable = false)
    private Integer danoBase;

    @Column(name = "precision_base", nullable = false)
    private Integer precisionBase;

    @Column(name = "peso", nullable = false)
    private Integer peso;

    @Column(name = "precio", nullable = false)
    private Integer precio;
}
