package com.example.Studio21.repository;

import com.example.Studio21.model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    // true si la pieza está en el carrito de algún cliente.
    boolean existsByDetallesProductoId(Long productoId);
}
