package com.jousting.rpg.repository;

import com.jousting.rpg.model.entity.Arma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArmaRepository extends JpaRepository<Arma, Long> {
}
