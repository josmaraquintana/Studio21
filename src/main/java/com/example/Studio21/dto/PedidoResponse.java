package com.example.Studio21.dto;

import com.example.Studio21.enums.EstadoPedido;
import com.example.Studio21.enums.MetodoPago;
import com.example.Studio21.model.DireccionEnvio;
import com.example.Studio21.model.Pedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(Long id, String numeroPedido, LocalDateTime fechaCompra,
                             EstadoPedido estado, boolean confirmado, MetodoPago metodoPago,
                             BigDecimal total, Direccion direccionEnvio, List<Detalle> detalles,
                             String paqueteria, String numeroGuia,
                             LocalDateTime fechaEnvio, LocalDateTime fechaEntrega) {
    public record Detalle(Long productoId, String nombre, int cantidad,
                          BigDecimal precioUnitarioCompra, BigDecimal subtotal) {
    }

    public record Direccion(String destinatario, String telefono, String calle,
                            String numeroExterior, String colonia, String ciudad,
                            String estado, String codigoPostal) {
        static Direccion de(DireccionEnvio d) {
            return new Direccion(d.getDestinatario(), d.getTelefono(), d.getCalle(),
                    d.getNumeroExterior(), d.getColonia(), d.getCiudad(),
                    d.getEstado(), d.getCodigoPostal());
        }
    }

    public static PedidoResponse de(Pedido p) {
        return new PedidoResponse(p.getId(), p.getNumeroPedido(), p.getFechaCompra(), p.getEstado(),
                p.isConfirmado(), p.getMetodoPago(), p.getTotal(), Direccion.de(p.getDireccionEnvio()),
                p.getDetalles().stream().map(d -> new Detalle(d.getProducto().getId(),
                        d.getProducto().getNombre(), d.getCantidad(),
                        d.getPrecioUnitarioCompra(), d.getPrecioUnitarioCompra())).toList(),
                p.getPaqueteria(), p.getNumeroGuia(), p.getFechaEnvio(), p.getFechaEntrega());
    }
}
