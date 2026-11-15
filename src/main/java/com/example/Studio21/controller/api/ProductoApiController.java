package com.example.Studio21.controller.api;

import com.example.Studio21.dto.ProductoDetalleDTO;
import com.example.Studio21.dto.ProductoResumenDTO;
import com.example.Studio21.enums.CondicionProducto;
import com.example.Studio21.enums.EstadoDisponibilidad;
import com.example.Studio21.service.CatalogoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/** Catálogo público (sin JWT). Lo consumen catalogo.html y producto.html. */
@RestController
@RequestMapping("/api/productos")
public class ProductoApiController {

    private final CatalogoService catalogo;

    public ProductoApiController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    // catalogo.html: buscador, filtros y "Ordenar por". Todos los parámetros son opcionales.
    @GetMapping
    public List<ProductoResumenDTO> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long artistaId,
            @RequestParam(required = false) CondicionProducto condicion,
            @RequestParam(required = false) EstadoDisponibilidad disponibilidad,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) String orden) {
        return catalogo.buscar(texto, categoriaId, artistaId, condicion, disponibilidad, precioMax, orden);
    }

    // producto.html: detalle de una pieza.
    @GetMapping("/{id}")
    public ProductoDetalleDTO detalle(@PathVariable Long id) {
        return catalogo.detalle(id);
    }
}
