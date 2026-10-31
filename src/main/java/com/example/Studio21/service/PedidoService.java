package com.example.Studio21.service;

import com.example.Studio21.enums.EstadoPedido;
import com.example.Studio21.model.DetallePedido;
import com.example.Studio21.model.Pedido;
import com.example.Studio21.model.Usuario;
import com.example.Studio21.repository.PedidoRepository;
import com.example.Studio21.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

// Gestión de pedidos desde el panel: pago, preparación, envío, entrega y cancelación.
// Las reglas de cada cambio de estado viven en la entidad Pedido; aquí se coordinan y se guardan.
@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioService inventarioService;

    public List<Pedido> buscar(String texto, EstadoPedido estado) {
        String filtro = (texto == null || texto.isBlank()) ? null : texto.trim();
        return pedidoRepository.buscar(filtro, estado);
    }

    // Los cinco pedidos más recientes para el resumen.
    public List<Pedido> recientes() {
        return pedidoRepository.obtenerRecientes().stream().limit(5).toList();
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    // Pagar convierte las piezas apartadas en vendidas.
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void registrarPago(Long id) {
        Pedido pedido = obtenerPedido(id);
        for (DetallePedido detalle : detallesOrdenados(pedido)) {
            inventarioService.exigirExistencia(detalle.getProducto().getId());
        }
        pedido.pagar();
        guardarConPiezas(pedido);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void comenzarPreparacion(Long id) {
        Pedido pedido = obtenerPedido(id);
        pedido.procesar();
        pedidoRepository.save(pedido);
    }

    // Al despachar se registra la salida física (-1) de cada pieza del pedido.
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void registrarEnvio(Long id, String paqueteria, String numeroGuia, String fechaEnvio, Usuario admin) {
        if (admin == null) {
            throw new IllegalStateException("No hay un administrador en sesión.");
        }
        Pedido pedido = obtenerPedido(id);
        LocalDateTime fecha = convertirFecha(fechaEnvio, "envío");
        pedido.enviar(paqueteria == null ? null : paqueteria.trim(),
                numeroGuia == null ? null : numeroGuia.trim(), fecha);
        pedidoRepository.save(pedido);

        for (DetallePedido detalle : detallesOrdenados(pedido)) {
            inventarioService.registrarDespacho(detalle.getProducto().getId(), admin,
                    "Despacho " + pedido.getNumeroPedido(), fecha);
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void registrarEntrega(Long id, String fechaEntrega) {
        Pedido pedido = obtenerPedido(id);
        pedido.entregar(convertirFecha(fechaEntrega, "entrega"));
        pedidoRepository.save(pedido);
    }

    // Cancelar un pedido pendiente libera las piezas apartadas, sin generar entradas físicas.
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void cancelar(Long id) {
        Pedido pedido = obtenerPedido(id);
        pedido.cancelar();
        guardarConPiezas(pedido);
    }

    private Pedido obtenerPedido(Long id) {
        return pedidoRepository.buscarParaActualizar(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado."));
    }

    // Las piezas cambian de disponibilidad junto con el pedido; se guardan ambas.
    private void guardarConPiezas(Pedido pedido) {
        for (DetallePedido detalle : detallesOrdenados(pedido)) {
            productoRepository.save(detalle.getProducto());
        }
        pedidoRepository.save(pedido);
    }

    private List<DetallePedido> detallesOrdenados(Pedido pedido) {
        return pedido.getDetalles().stream()
                .sorted(java.util.Comparator.comparing(detalle -> detalle.getProducto().getId())).toList();
    }

    // El input datetime-local envía el formato 2024-10-24T10:15.
    private LocalDateTime convertirFecha(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("La fecha de " + campo + " es obligatoria.");
        }
        try {
            return LocalDateTime.parse(texto);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La fecha de " + campo + " no es válida.");
        }
    }
}
