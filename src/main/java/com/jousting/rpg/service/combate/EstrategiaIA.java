package com.jousting.rpg.service.combate;

import com.jousting.rpg.dto.combate.DecisionPasadaDTO;
import com.jousting.rpg.dto.combate.EstadoCaballero;

public interface EstrategiaIA {

    DecisionPasadaDTO decidir(EstadoCaballero ia, EstadoCaballero jugador);
}
