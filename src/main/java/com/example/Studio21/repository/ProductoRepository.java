package com.example.Studio21.repository;

import com.example.Studio21.enums.CondicionProducto;
import com.example.Studio21.enums.EstadoDisponibilidad;
import com.example.Studio21.enums.EstadoVerificacion;
import com.example.Studio21.model.Producto;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Filtros opcionales: si un parámetro llega en null, ese filtro se ignora.
    @Query("SELECT p FROM Producto p WHERE " +
           "(:texto IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) " +
           "OR LOWER(p.artista.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))) AND " +
           "(:verificacion IS NULL OR p.estadoVerificacion = :verificacion) AND " +
           "(:disponibilidad IS NULL OR p.estadoDisponibilidad = :disponibilidad) " +
           "ORDER BY p.id DESC")
    List<Producto> buscar(@Param("texto") String texto,
                          @Param("verificacion") EstadoVerificacion verificacion,
                          @Param("disponibilidad") EstadoDisponibilidad disponibilidad);

    // Piezas por estado de autenticidad (por ejemplo, las pendientes de revisión).
    List<Producto> findByEstadoVerificacionOrderByIdAsc(EstadoVerificacion estadoVerificacion);

    List<Producto> findAllByOrderByNombreAsc();

    // Contadores para el resumen del panel.
    long countByEstadoVerificacion(EstadoVerificacion estadoVerificacion);

    long countByEstadoDisponibilidad(EstadoDisponibilidad estadoDisponibilidad);

    long countByActivoAndEstadoDisponibilidad(boolean activo, EstadoDisponibilidad estadoDisponibilidad);

    // Se usan antes de eliminar una categoría o un artista.
    boolean existsByCategoriaId(Long categoriaId);

    boolean existsByArtistaId(Long artistaId);

    // ---- Catálogo público (API) ----
    // Visible = verificada y publicada, o ya vendida (el detalle sigue mostrando "Vendida").
    // Los filtros que llegan en null se ignoran; el orden llega en el Sort.
    @Query("SELECT p FROM Producto p WHERE " +
           "p.estadoVerificacion = com.example.Studio21.enums.EstadoVerificacion.VERIFICADA AND " +
           "(p.activo = true OR p.estadoDisponibilidad = com.example.Studio21.enums.EstadoDisponibilidad.VENDIDA) AND " +
           "(:texto IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) " +
           "OR LOWER(p.artista.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))) AND " +
           "(:categoriaId IS NULL OR p.categoria.id = :categoriaId) AND " +
           "(:artistaId IS NULL OR p.artista.id = :artistaId) AND " +
           "(:condicion IS NULL OR p.condicion = :condicion) AND " +
           "(:disponibilidad IS NULL OR p.estadoDisponibilidad = :disponibilidad) AND " +
           "(:precioMax IS NULL OR p.precioReventa <= :precioMax)")
    List<Producto> buscarPublico(@Param("texto") String texto,
                                 @Param("categoriaId") Long categoriaId,
                                 @Param("artistaId") Long artistaId,
                                 @Param("condicion") CondicionProducto condicion,
                                 @Param("disponibilidad") EstadoDisponibilidad disponibilidad,
                                 @Param("precioMax") BigDecimal precioMax,
                                 Sort sort);

    @Query("SELECT p FROM Producto p WHERE p.id = :id AND " +
           "p.estadoVerificacion = com.example.Studio21.enums.EstadoVerificacion.VERIFICADA AND " +
           "(p.activo = true OR p.estadoDisponibilidad = com.example.Studio21.enums.EstadoDisponibilidad.VENDIDA)")
    Optional<Producto> buscarPublicoPorId(@Param("id") Long id);
}
