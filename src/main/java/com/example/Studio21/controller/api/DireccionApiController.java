package com.example.Studio21.controller.api;

import com.example.Studio21.dto.DireccionDTO;
import com.example.Studio21.dto.DireccionRequestDTO;
import com.example.Studio21.security.UsuarioActual;
import com.example.Studio21.service.DireccionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Direcciones del cliente (necesita JWT). Las consumen direcciones.html y checkout.html. */
@RestController
@RequestMapping("/api/direcciones")
public class DireccionApiController {

    private final DireccionService direcciones;
    private final UsuarioActual usuarioActual;

    public DireccionApiController(DireccionService direcciones, UsuarioActual usuarioActual) {
        this.direcciones = direcciones;
        this.usuarioActual = usuarioActual;
    }

    // direcciones.html (tarjetas) y checkout.html (paso 1, elegir dirección).
    @GetMapping
    public List<DireccionDTO> listar() {
        return direcciones.listar(usuarioActual.id());
    }

    // direcciones.html: botón "Guardar dirección".
    @PostMapping
    public ResponseEntity<DireccionDTO> crear(@Valid @RequestBody DireccionRequestDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(direcciones.crear(usuarioActual.id(), datos));
    }

    // direcciones.html: botón "Editar dirección".
    @PutMapping("/{id}")
    public DireccionDTO actualizar(@PathVariable Long id, @Valid @RequestBody DireccionRequestDTO datos) {
        return direcciones.actualizar(usuarioActual.id(), id, datos);
    }

    // direcciones.html: quitar una dirección guardada.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        direcciones.eliminar(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }
}