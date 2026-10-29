package com.example.Studio21.repository;

import com.example.Studio21.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    // Movimientos más recientes primero.
    List<MovimientoInventario> findAllByOrderByFechaDesc();

    List<MovimientoInventario> findByProductoId(Long productoId);
}
