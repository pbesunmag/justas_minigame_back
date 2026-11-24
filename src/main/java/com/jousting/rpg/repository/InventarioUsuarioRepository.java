package com.jousting.rpg.repository;

import com.jousting.rpg.model.entity.InventarioUsuario;
import com.jousting.rpg.model.enums.TipoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventarioUsuarioRepository extends JpaRepository<InventarioUsuario, Long> {

    List<InventarioUsuario> findByUsuarioId(Long usuarioId);

    List<InventarioUsuario> findByUsuarioIdAndTipoItem(Long usuarioId, TipoItem tipoItem);

    boolean existsByUsuarioIdAndTipoItemAndItemId(Long usuarioId, TipoItem tipoItem, Long itemId);
}
