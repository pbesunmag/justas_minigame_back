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
@Table(name = "caballos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Caballo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "velocidad_max", nullable = false)
    private Integer velocidadMax;

    @Column(name = "resistencia_max", nullable = false)
    private Integer resistenciaMax;

    @Column(name = "consumo_stamina", nullable = false)
    private Integer consumoStamina;

    @Column(name = "precio", nullable = false)
    private Integer precio;
}
