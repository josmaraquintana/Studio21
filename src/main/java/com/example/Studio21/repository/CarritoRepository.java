package com.example.Studio21.repository;

import com.example.Studio21.model.Carrito;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    Optional<Carrito> findByUsuarioId(Long usuarioId);

    @Query(
            "select d.producto.id from DetalleCarrito d where d.carrito.id = :id order by d.producto.id")
    List<Long> productoIds(@Param("id") Long id);

    // true si la pieza está en el carrito de algún cliente.
    boolean existsByDetallesProductoId(Long productoId);
}
