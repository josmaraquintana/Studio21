package com.example.Studio21.dto;

import com.example.Studio21.enums.EstadoPedido;
import com.example.Studio21.enums.MetodoPago;
import com.example.Studio21.model.Pedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoResumenResponse(Long id, String numeroPedido, LocalDateTime fechaCompra,
                                    EstadoPedido estado, MetodoPago metodoPago,
                                    BigDecimal total, int piezas) {
    public static PedidoResumenResponse de(Pedido p) {
        return new PedidoResumenResponse(p.getId(), p.getNumeroPedido(), p.getFechaCompra(),
                p.getEstado(), p.getMetodoPago(), p.getTotal(), p.getDetalles().size());
    }
}
