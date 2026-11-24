package com.jousting.rpg.repository;

import com.jousting.rpg.model.entity.Caballero;
import com.jousting.rpg.model.enums.TipoCaballero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaballeroRepository extends JpaRepository<Caballero, Long> {

    Optional<Caballero> findByUsuarioId(Long usuarioId);

    List<Caballero> findByTipo(TipoCaballero tipo);
}
