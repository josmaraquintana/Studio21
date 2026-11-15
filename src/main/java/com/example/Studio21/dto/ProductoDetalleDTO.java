package com.example.Studio21.dto;

import java.math.BigDecimal;
import java.util.List;

/** Pantalla de detalle (producto.html). No incluye datos internos: donación, verificador ni evidencia. */
public record ProductoDetalleDTO(
        Long id,
        String nombre,
        String descripcion,
        Long artistaId,
        String artista,
        Long categoriaId,
        String categoria,
        String talla,
        String condicion,
        String detalleCondicion,
        BigDecimal precioOriginal,
        BigDecimal precioReventa,
        String disponibilidad,
        List<String> imagenes) {
}
