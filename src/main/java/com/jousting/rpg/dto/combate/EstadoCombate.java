package com.jousting.rpg.dto.combate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoCombate {

    private String combateId;
    private Long usuarioId;
    private int pasadaActual;
    private EstadoResultadoCombate estado;

    @Builder.Default
    private List<String> logNarrativo = new ArrayList<>();

    private EstadoCaballero jugador;
    private EstadoCaballero rival;
}
