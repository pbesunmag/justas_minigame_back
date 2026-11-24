package com.jousting.rpg.api.controller;

import com.jousting.rpg.dto.combate.DecisionPasadaDTO;
import com.jousting.rpg.dto.combate.EstadoCombate;
import com.jousting.rpg.service.combate.CombateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para el motor de combate táctico por pasadas de justa.
 */
@RestController
@RequestMapping("/api/combate")
@Tag(name = "Combate Táctico", description = "Gestión en memoria de pasadas de justa por turnos")
public class CombateController {

    private final CombateService combateService;

    public CombateController(CombateService combateService) {
        this.combateService = combateService;
    }

    @PostMapping("/iniciar")
    public ResponseEntity<EstadoCombate> iniciarCombate(
            @RequestParam Long usuarioId,
            @RequestParam Long rivalId
    ) {
        EstadoCombate estadoInicial = combateService.iniciarCombate(usuarioId, rivalId);
        return ResponseEntity.ok(estadoInicial);
    }

    @PostMapping("/{combateId}/pasada")
    public ResponseEntity<EstadoCombate> ejecutarPasada(
            @PathVariable String combateId,
            @RequestBody DecisionPasadaDTO decisionJugador
    ) {
        EstadoCombate estadoActualizado = combateService.ejecutarPasada(combateId, decisionJugador);
        return ResponseEntity.ok(estadoActualizado);
    }
}
