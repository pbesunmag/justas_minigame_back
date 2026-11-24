package com.jousting.rpg.repository;

import com.jousting.rpg.model.entity.Escudo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EscudoRepository extends JpaRepository<Escudo, Long> {
}
