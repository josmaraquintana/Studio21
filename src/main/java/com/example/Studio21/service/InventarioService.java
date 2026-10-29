package com.example.Studio21.service;

import com.example.Studio21.model.MovimientoInventario;
import com.example.Studio21.model.Producto;
import com.example.Studio21.model.Usuario;
import com.example.Studio21.repository.MovimientoInventarioRepository;
import com.example.Studio21.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public void registrarAjuste(Long productoId, Integer cambio, String motivo, Usuario admin) {
        if (admin == null) {
            throw new IllegalStateException("No hay un administrador en sesión.");
        }
        if (productoId == null) {
            throw new IllegalArgumentException("Selecciona la pieza.");
        }
        Producto producto = productoRepository.findById(productoId)
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
        movimientoRepository.save(new MovimientoInventario(producto, admin, cambio, motivo.trim(), LocalDateTime.now()));
    }
}
