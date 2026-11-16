package com.example.Studio21.controller.cliente;

import com.example.Studio21.dto.CrearPedidoRequest;
import com.example.Studio21.dto.PedidoResponse;
import com.example.Studio21.dto.PedidoResumenResponse;
import com.example.Studio21.service.PedidoClienteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoClienteController {
    private final PedidoClienteService pedidos;

    public PedidoClienteController(PedidoClienteService pedidos) {
        this.pedidos = pedidos;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> confirmar(@Valid @RequestBody CrearPedidoRequest datos) {
        PedidoResponse pedido = pedidos.confirmar(datos);
        return ResponseEntity.created(URI.create("/api/pedidos/" + pedido.id())).body(pedido);
    }

    @GetMapping
    public List<PedidoResumenResponse> listar() {
        return pedidos.listar();
    }

    @GetMapping("/{id}")
    public PedidoResponse detalle(@PathVariable @Positive Long id) {
        return pedidos.detalle(id);
    }

    @PatchMapping("/{id}/cancelacion")
    public PedidoResponse cancelar(@PathVariable @Positive Long id) {
        return pedidos.cancelar(id);
    }
}
