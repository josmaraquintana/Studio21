package com.example.Studio21.dto;

import com.example.Studio21.enums.MetodoPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CrearPedidoRequest(
        @NotNull(message = "La dirección es obligatoria")
        @Positive(message = "La dirección debe tener un ID positivo") Long direccionId,
        @NotNull(message = "El método de pago es obligatorio") MetodoPago metodoPago) {
}
