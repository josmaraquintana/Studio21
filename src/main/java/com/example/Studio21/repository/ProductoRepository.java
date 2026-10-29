package com.example.Studio21.repository;

import com.example.Studio21.enums.EstadoDisponibilidad;
import com.example.Studio21.enums.EstadoVerificacion;
import com.example.Studio21.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
