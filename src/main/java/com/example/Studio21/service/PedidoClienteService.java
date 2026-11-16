package com.example.Studio21.service;

import com.example.Studio21.dto.CrearPedidoRequest;
import com.example.Studio21.dto.PedidoResponse;
import com.example.Studio21.dto.PedidoResumenResponse;
import com.example.Studio21.exception.ConflictException;
import com.example.Studio21.exception.NotFoundException;
import com.example.Studio21.model.Carrito;
import com.example.Studio21.model.Direccion;
import com.example.Studio21.model.Pedido;
import com.example.Studio21.model.Producto;
import com.example.Studio21.model.Usuario;
import com.example.Studio21.repository.CarritoRepository;
import com.example.Studio21.repository.DireccionRepository;
import com.example.Studio21.repository.PedidoRepository;
import com.example.Studio21.repository.ProductoRepository;
import com.example.Studio21.repository.UsuarioRepository;
import com.example.Studio21.security.UsuarioActual;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoClienteService {
    private final PedidoRepository pedidos;
    private final CarritoRepository carritos;
    private final ProductoRepository productos;
    private final DireccionRepository direcciones;
    private final UsuarioRepository usuarios;
    private final UsuarioActual usuarioActual;
    private final InventarioService inventario;

    public PedidoClienteService(PedidoRepository pedidos, CarritoRepository carritos,
                                ProductoRepository productos, DireccionRepository direcciones,
                                UsuarioRepository usuarios, UsuarioActual usuarioActual, InventarioService inventario) {
        this.pedidos = pedidos;
        this.carritos = carritos;
        this.productos = productos;
        this.direcciones = direcciones;
        this.usuarios = usuarios;
        this.usuarioActual = usuarioActual;
        this.inventario = inventario;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PedidoResponse confirmar(CrearPedidoRequest datos) {
        Usuario usuario = usuarios.buscarParaActualizar(usuarioActual.id())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("La cuenta de este token ya no existe"));
        Direccion direccion = direcciones.findByIdAndUsuarioId(datos.direccionId(), usuario.getId())
                .orElseThrow(() -> new NotFoundException("La dirección solicitada no existe"));
        Carrito carrito = carritos.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ConflictException("Tu carrito está vacío"));
        // Solo consultar IDs evita cargar estados de piezas antes de obtener sus bloqueos.
        List<Long> ids = carritos.productoIds(carrito.getId());
        if (ids.isEmpty()) {
            throw new ConflictException("Tu carrito está vacío");
        }
        List<Producto> piezas = new ArrayList<>();
        for (Long id : ids) {
            Producto producto = productos.buscarParaActualizar(id)
                    .orElseThrow(() -> new NotFoundException("Una pieza del carrito ya no existe"));
            if (!producto.estaDisponibleParaVenta()) {
                throw new ConflictException("La pieza " + id + " ya no está disponible para venta");
            }
            inventario.exigirExistencia(id);
            piezas.add(producto);
        }
        String numero = "S21-" + UUID.randomUUID().toString().replace("-", "").substring(0, 26);
        Pedido pedido = new Pedido(numero, usuario, direccion, datos.metodoPago());
        piezas.forEach(pedido::agregarProducto);
        pedido.confirmar();
        pedidos.save(pedido);
        carrito.vaciar();
        return PedidoResponse.de(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResumenResponse> listar() {
        return pedidos.findByUsuarioIdOrderByIdDesc(usuarioActual.obtener().getId()).stream()
                .map(PedidoResumenResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse detalle(Long id) {
        return PedidoResponse.de(pedidos.findByIdAndUsuarioId(id, usuarioActual.obtener().getId())
                .orElseThrow(() -> new NotFoundException("El pedido solicitado no existe")));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PedidoResponse cancelar(Long id) {
        Pedido pedido = pedidos.buscarParaActualizarDelUsuario(id, usuarioActual.obtener().getId())
                .orElseThrow(() -> new NotFoundException("El pedido solicitado no existe"));
        for (Long productoId : pedidos.productoIds(pedido.getId())) {
            productos.buscarParaActualizar(productoId)
                    .orElseThrow(() -> new NotFoundException("Una pieza del pedido ya no existe"));
        }
        pedido.cancelar();
        return PedidoResponse.de(pedido);
    }
}
