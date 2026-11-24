package com.jousting.rpg.repository;

import com.jousting.rpg.model.entity.Caballo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaballoRepository extends JpaRepository<Caballo, Long> {
}
