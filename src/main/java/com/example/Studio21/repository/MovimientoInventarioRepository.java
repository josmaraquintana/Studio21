package com.example.Studio21.repository;

import com.example.Studio21.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    @Query("select coalesce(sum(m.cambioCantidad), 0) from MovimientoInventario m where m.producto.id = :id")
    long saldoFisico(@Param("id") Long id);

    // Movimientos más recientes primero.
    List<MovimientoInventario> findAllByOrderByFechaDesc();

    List<MovimientoInventario> findByProductoId(Long productoId);
}
