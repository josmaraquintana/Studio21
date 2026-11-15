package com.example.Studio21.service;

import com.example.Studio21.dto.ArtistaDTO;
import com.example.Studio21.dto.CategoriaDTO;
import com.example.Studio21.dto.ProductoDetalleDTO;
import com.example.Studio21.dto.ProductoResumenDTO;
import com.example.Studio21.enums.CondicionProducto;
import com.example.Studio21.enums.EstadoDisponibilidad;
import com.example.Studio21.exception.NotFoundException;
import com.example.Studio21.model.ImagenProducto;
import com.example.Studio21.model.Producto;
import com.example.Studio21.repository.ArtistaRepository;
import com.example.Studio21.repository.CategoriaRepository;
import com.example.Studio21.repository.ProductoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lectura pública del catálogo. Solo expone piezas verificadas y publicadas (o ya vendidas,
 * para que el detalle siga mostrando "Vendida"). Nunca devuelve entidades: todo sale como DTO.
 */
@Service
@Transactional(readOnly = true)
public class CatalogoService {

    private final ProductoRepository productos;
    private final CategoriaRepository categorias;
    private final ArtistaRepository artistas;

    public CatalogoService(ProductoRepository productos, CategoriaRepository categorias, ArtistaRepository artistas) {
        this.productos = productos;
        this.categorias = categorias;
        this.artistas = artistas;
    }

    public List<ProductoResumenDTO> buscar(String texto, Long categoriaId, Long artistaId,
                                           CondicionProducto condicion, EstadoDisponibilidad disponibilidad,
                                           BigDecimal precioMax, String orden) {
        if (precioMax != null && precioMax.signum() < 0) {
            throw new IllegalArgumentException("El precio máximo no puede ser negativo");
        }
        String textoLimpio = (texto == null || texto.isBlank()) ? null : texto.trim();
        return productos.buscarPublico(textoLimpio, categoriaId, artistaId, condicion, disponibilidad,
                        precioMax, ordenar(orden))
                .stream().map(this::aResumen).toList();
    }

    public ProductoDetalleDTO detalle(Long id) {
        Producto p = productos.buscarPublicoPorId(id)
                .orElseThrow(() -> new NotFoundException("La pieza solicitada no existe"));
        return new ProductoDetalleDTO(
                p.getId(), p.getNombre(), p.getDescripcion(),
                p.getArtista().getId(), p.getArtista().getNombre(),
                p.getCategoria().getId(), p.getCategoria().getNombre(),
                p.getTalla(), p.getCondicion().name(), p.getDetalleCondicion(),
                p.getPrecioOriginal(), p.getPrecioReventa(), p.getEstadoDisponibilidad().name(),
                p.getImagenes().stream().map(ImagenProducto::getUrl).toList());
    }

    public List<CategoriaDTO> listarCategorias() {
        return categorias.findAllByOrderByNombreAsc().stream()
                .map(c -> new CategoriaDTO(c.getId(), c.getNombre(), c.getDescripcion(), c.isRopa()))
                .toList();
    }

    public List<ArtistaDTO> listarArtistas() {
        return artistas.findAllByOrderByNombreAsc().stream()
                .map(a -> new ArtistaDTO(a.getId(), a.getNombre(), a.getGeneroMusical()))
                .toList();
    }

    // Equivale al select "Ordenar por" del catálogo.
    private Sort ordenar(String orden) {
        if (orden == null || orden.isBlank() || orden.equals("recientes")) {
            return Sort.by(Sort.Direction.DESC, "id");
        }
        return switch (orden) {
            case "precio_asc" -> Sort.by(Sort.Direction.ASC, "precioReventa");
            case "nombre" -> Sort.by(Sort.Direction.ASC, "nombre");
            default -> throw new IllegalArgumentException(
                    "Orden no válido. Usa: recientes, precio_asc o nombre");
        };
    }

    private ProductoResumenDTO aResumen(Producto p) {
        String imagen = p.getImagenes().isEmpty() ? null : p.getImagenes().get(0).getUrl();
        return new ProductoResumenDTO(p.getId(), p.getNombre(), p.getArtista().getNombre(),
                p.getCategoria().getNombre(), p.getCondicion().name(),
                p.getEstadoDisponibilidad().name(), p.getPrecioReventa(), imagen);
    }
}
