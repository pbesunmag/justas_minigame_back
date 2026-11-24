package com.jousting.rpg.repository;

import com.jousting.rpg.model.entity.Armadura;
import com.jousting.rpg.model.enums.ZonaArmadura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArmaduraRepository extends JpaRepository<Armadura, Long> {

    List<Armadura> findByZona(ZonaArmadura zona);
}
