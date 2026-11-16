package com.example.Studio21.service;

import com.example.Studio21.dto.CarritoResponse;
import com.example.Studio21.exception.ConflictException;
import com.example.Studio21.exception.NotFoundException;
import com.example.Studio21.model.Carrito;
import com.example.Studio21.model.DetalleCarrito;
import com.example.Studio21.model.Producto;
import com.example.Studio21.model.Usuario;
import com.example.Studio21.repository.CarritoRepository;
import com.example.Studio21.repository.ProductoRepository;
import com.example.Studio21.repository.UsuarioRepository;
import com.example.Studio21.security.UsuarioActual;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarritoService {
    private final CarritoRepository carritos;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;
    private final UsuarioActual usuarioActual;
    private final InventarioService inventario;

    public CarritoService(CarritoRepository carritos, ProductoRepository productos,
                          UsuarioRepository usuarios, UsuarioActual usuarioActual, InventarioService inventario) {
        this.carritos = carritos;
        this.productos = productos;
        this.usuarios = usuarios;
        this.usuarioActual = usuarioActual;
        this.inventario = inventario;
    }

    @Transactional(readOnly = true)
    public CarritoResponse obtener() {
        Long usuarioId = usuarioActual.obtener().getId();
        return carritos.findByUsuarioId(usuarioId).map(CarritoResponse::de).orElseGet(CarritoResponse::vacio);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CarritoResponse agregar(Long productoId) {
        Usuario usuario = bloquearUsuario();
        Carrito carrito = carritos.findByUsuarioId(usuario.getId()).orElseGet(() -> new Carrito(usuario));
        Producto producto = productos.buscarParaActualizar(productoId)
                .orElseThrow(() -> new NotFoundException("La pieza solicitada no existe"));
        if (!producto.estaDisponibleParaVenta()) {
            throw new ConflictException("La pieza no está publicada, verificada y disponible para venta");
        }
        inventario.exigirExistencia(productoId);
        if (carrito.getDetalles().stream().anyMatch(d -> d.getProducto().getId().equals(productoId))) {
            throw new ConflictException("La pieza ya está en tu carrito");
        }
        carrito.agregarProducto(producto);
        carritos.save(carrito);
        return CarritoResponse.de(carrito);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CarritoResponse quitar(Long productoId) {
        Usuario usuario = bloquearUsuario();
        Carrito carrito = carritos.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new NotFoundException("La pieza no está en tu carrito"));
        DetalleCarrito detalle = carrito.getDetalles().stream()
                .filter(d -> d.getProducto().getId().equals(productoId)).findFirst()
                .orElseThrow(() -> new NotFoundException("La pieza no está en tu carrito"));
        carrito.eliminarDetalle(detalle);
        return CarritoResponse.de(carrito);
    }

    private Usuario bloquearUsuario() {
        // También serializa el primer carrito, cuando todavía no hay una fila que bloquear.
        return usuarios.buscarParaActualizar(usuarioActual.id())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("La cuenta de este token ya no existe"));
    }
}
