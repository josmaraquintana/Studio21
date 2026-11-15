package com.example.Studio21.controller.cliente;

import com.example.Studio21.dto.CuentaRequest;
import com.example.Studio21.dto.CuentaResponse;
import com.example.Studio21.service.CuentaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Datos del usuario con sesion abierta. Los consume cuenta. ("Mis datos").
 * La URL no lleva id: la cuenta es siempre la del token (necesita JWT).
 */
@RestController
@RequestMapping("/api/cuenta")
public class CuentaController {
    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping
    public CuentaResponse verCuenta() {
        return cuentaService.obtener();
    }

    @PutMapping
    public CuentaResponse actualizarCuenta(@Valid @RequestBody CuentaRequest datos) {
        return cuentaService.actualizar(datos);
    }
}

