package com.example.Studio21.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AgregarCarritoRequest(
        @NotNull(message = "El producto es obligatorio")
        @Positive(message = "El producto debe tener un ID positivo") Long productoId) {
}
