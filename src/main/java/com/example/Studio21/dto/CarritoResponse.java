package com.example.Studio21.dto;

import com.example.Studio21.enums.EstadoDisponibilidad;
import com.example.Studio21.model.Carrito;
import com.example.Studio21.model.Producto;
import java.math.BigDecimal;
import java.util.List;

public record CarritoResponse(List<Item> items, BigDecimal total) {
    public record Item(Long productoId, String nombre, String imagenUrl, int cantidad,
                       BigDecimal precioUnitario, BigDecimal subtotal,
                       EstadoDisponibilidad estadoDisponibilidad) {
    }

    public static CarritoResponse vacio() {
        return new CarritoResponse(List.of(), BigDecimal.ZERO);
    }

    public static CarritoResponse de(Carrito carrito) {
        List<Item> items = carrito.getDetalles().stream().map(detalle -> {
            Producto p = detalle.getProducto();
            return new Item(p.getId(), p.getNombre(),
                    p.getImagenes().isEmpty() ? null : p.getImagenes().get(0).getUrl(),
                    detalle.getCantidad(), p.getPrecioReventa(), p.getPrecioReventa(),
                    p.getEstadoDisponibilidad());
        }).toList();
        return new CarritoResponse(items,
                items.stream().map(Item::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
