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
@Table(name = "escudos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Escudo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "durabilidad_max", nullable = false)
    private Integer durabilidadMax;

    @Column(name = "absorcion_dano", nullable = false)
    private Integer absorcionDano;

    @Column(name = "peso", nullable = false)
    private Integer peso;

    @Column(name = "precio", nullable = false)
    private Integer precio;
}
