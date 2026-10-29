package com.example.Studio21.repository;

import com.example.Studio21.enums.EstadoPedido;
import com.example.Studio21.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Busca por número de pedido o nombre del cliente y filtra por estado; ambos filtros son opcionales.
    @Query("SELECT p FROM Pedido p WHERE " +
           "(:texto IS NULL OR LOWER(p.numeroPedido) LIKE LOWER(CONCAT('%', :texto, '%')) " +
           "OR LOWER(p.usuario.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))) AND " +
           "(:estado IS NULL OR p.estado = :estado) " +
           "ORDER BY p.id DESC")
    List<Pedido> buscar(@Param("texto") String texto, @Param("estado") EstadoPedido estado);

    // Pedidos más recientes primero.
    @Query("SELECT p FROM Pedido p ORDER BY p.id DESC")
    List<Pedido> obtenerRecientes();

    // true si la pieza aparece en algún pedido.
    boolean existsByDetallesProductoId(Long productoId);
}
