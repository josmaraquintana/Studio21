package com.example.Studio21.service;

import com.example.Studio21.model.MovimientoInventario;
import com.example.Studio21.enums.EstadoDisponibilidad;
import com.example.Studio21.enums.RolUsuario;
import com.example.Studio21.model.Producto;
import com.example.Studio21.model.Usuario;
import com.example.Studio21.repository.MovimientoInventarioRepository;
import com.example.Studio21.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.time.LocalDateTime;
import java.util.List;

// Movimientos físicos de inventario: cada uno es la entrada (+1) o salida (-1) de una pieza única.
@Service
public class InventarioService {

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public List<MovimientoInventario> listar() {
        return movimientoRepository.findAllByOrderByFechaDesc();
    }

    // Ajuste manual: documenta una entrada o salida física real y siempre requiere motivo.
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void registrarAjuste(Long productoId, Integer cambio, String motivo, Usuario admin) {
        if (admin == null) {
            throw new IllegalStateException("No hay un administrador en sesión.");
        }
        if (productoId == null) {
            throw new IllegalArgumentException("Selecciona la pieza.");
        }
        Producto producto = productoRepository.buscarParaActualizar(productoId)
                .orElseThrow(() -> new IllegalArgumentException("La pieza seleccionada no existe."));
        if (cambio == null || (cambio != 1 && cambio != -1)) {
            throw new IllegalArgumentException("Una pieza única solo admite entrada (+1) o salida (-1) de una unidad.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El ajuste requiere un motivo.");
        }
        if (motivo.trim().length() > 200) {
            throw new IllegalArgumentException("El motivo admite máximo 200 caracteres.");
        }
        exigirAdministrador(admin);
        if (producto.getEstadoDisponibilidad() != EstadoDisponibilidad.DISPONIBLE || producto.getPedidoReserva() != null) {
            throw new IllegalStateException("No se puede ajustar una pieza apartada o vendida. Usa el flujo de pedidos.");
        }
        registrarMovimiento(producto, admin, cambio, motivo.trim(), LocalDateTime.now());
        if (cambio == -1) {
            producto.retirarPublicacion(); // La pérdida impide ofrecerla; no cambia una venta ni una reserva.
        }
    }

    // Todas las operaciones físicas bloquean la misma fila de producto antes de leer el saldo.
    // READ_COMMITTED permite leer el saldo recién confirmado al obtener el bloqueo en MySQL.
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void exigirExistencia(Long productoId) {
        obtenerBloqueado(productoId);
        if (movimientoRepository.saldoFisico(productoId) != 1) {
            throw new IllegalStateException("La pieza no tiene existencia física para venta.");
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void registrarRecepcion(Long productoId, Usuario admin, String motivo) {
        exigirAdministrador(admin);
        Producto producto = obtenerBloqueado(productoId);
        if (!movimientoRepository.findByProductoId(productoId).isEmpty()) {
            throw new IllegalStateException("La recepción de esta pieza ya fue registrada.");
        }
        registrarMovimiento(producto, admin, 1, motivo, LocalDateTime.now());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void registrarDespacho(Long productoId, Usuario admin, String motivo, LocalDateTime fecha) {
        exigirAdministrador(admin);
        Producto producto = obtenerBloqueado(productoId);
        if (producto.getEstadoDisponibilidad() != EstadoDisponibilidad.VENDIDA) {
            throw new IllegalStateException("Solo se despachan piezas vendidas mediante un pedido.");
        }
        registrarMovimiento(producto, admin, -1, motivo, fecha);
    }

    private Producto obtenerBloqueado(Long id) {
        return productoRepository.buscarParaActualizar(id)
                .orElseThrow(() -> new IllegalArgumentException("Pieza no encontrada."));
    }

    private void registrarMovimiento(Producto producto, Usuario admin, int cambio, String motivo, LocalDateTime fecha) {
        long saldo = movimientoRepository.saldoFisico(producto.getId());
        if (saldo < 0 || saldo > 1 || saldo + cambio < 0 || saldo + cambio > 1) {
            throw new IllegalStateException("El saldo físico de una pieza única solo puede ser 0 o 1.");
        }
        movimientoRepository.save(new MovimientoInventario(producto, admin, cambio, motivo, fecha));
    }

    private void exigirAdministrador(Usuario admin) {
        if (admin == null || admin.getRol() != RolUsuario.ADMIN) {
            throw new IllegalStateException("Se requiere un administrador.");
        }
    }
}
