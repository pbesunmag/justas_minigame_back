package com.jousting.rpg.model.entity;

import com.jousting.rpg.model.enums.ZonaArmadura;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "armaduras")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Armadura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "zona", nullable = false)
    private ZonaArmadura zona;

    @Column(name = "defensa", nullable = false)
    private Integer defensa;

    @Column(name = "hp_extra", nullable = false)
    private Integer hpExtra;

    @Column(name = "peso", nullable = false)
    private Integer peso;

    @Column(name = "precio", nullable = false)
    private Integer precio;
}
