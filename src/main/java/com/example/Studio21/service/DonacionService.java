package com.example.Studio21.service;

import com.example.Studio21.model.Donacion;
import com.example.Studio21.model.Donante;
import com.example.Studio21.model.Usuario;
import com.example.Studio21.repository.DonacionRepository;
import com.example.Studio21.repository.DonanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

// Registro de donaciones y de sus donantes.
@Service
public class DonacionService {

    @Autowired
    private DonacionRepository donacionRepository;

    @Autowired
    private DonanteRepository donanteRepository;

    public List<Donacion> listar() {
        return donacionRepository.findAllByOrderByFechaRecepcionDesc();
    }

    public Donacion buscarPorId(Long id) {
        return donacionRepository.findById(id).orElse(null);
    }

    public List<Donante> listarDonantes() {
        return donanteRepository.findAllByOrderByNombreAsc();
    }

    // Si llega donanteId se usa un donante existente; si no, se registra uno nuevo con sus datos.
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Donacion registrar(Long donanteId, String nombre, String telefono, String correo,
                              String fechaRecepcion, String observaciones, Usuario recibidoPor) {
        if (recibidoPor == null) {
            throw new IllegalStateException("No hay un administrador en sesión.");
        }

        // La fecha se valida antes de guardar cualquier dato.
        LocalDateTime fecha = convertirFecha(fechaRecepcion);
        if (fecha.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha de recepción no puede estar en el futuro.");
        }

        Donante donante;
        if (donanteId != null) {
            donante = donanteRepository.findById(donanteId)
                    .orElseThrow(() -> new IllegalArgumentException("El donante seleccionado no existe."));
        } else {
            if (nombre == null || nombre.isBlank() || telefono == null || telefono.isBlank()) {
                throw new IllegalArgumentException("Selecciona un donante o captura el nombre y teléfono del nuevo donante.");
            }
            String correoDonante = (correo == null || correo.isBlank()) ? null : correo.trim();
            donante = donanteRepository.save(new Donante(nombre.trim(), telefono.trim(), correoDonante));
        }

        String obs = (observaciones == null || observaciones.isBlank()) ? null : observaciones.trim();
        return donacionRepository.save(new Donacion(donante, fecha, recibidoPor, obs));
    }

    // Una donación solo se elimina si todavía no tiene piezas registradas.
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void eliminar(Long id) {
        Donacion donacion = donacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Donación no encontrada."));
        if (!donacion.getProductos().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar: la donación ya tiene piezas registradas.");
        }
        donacionRepository.deleteById(id);
    }

    // El input datetime-local envía el formato 2024-10-24T10:15.
    private LocalDateTime convertirFecha(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("La fecha de recepción es obligatoria.");
        }
        try {
            return LocalDateTime.parse(texto);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La fecha de recepción no es válida.");
        }
    }
}
