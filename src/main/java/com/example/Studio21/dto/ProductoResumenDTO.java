package com.example.Studio21.dto;

import java.math.BigDecimal;

/** Tarjeta del catálogo (catalogo.html): lo mínimo que necesita la lista de piezas. */
public record ProductoResumenDTO(
        Long id,
        String nombre,
        String artista,
        String categoria,
        String condicion,
        String disponibilidad,
        BigDecimal precio,
        String imagenUrl) {
}
