package com.example.Studio21.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DireccionRequestDTO(
        @NotBlank(message = "El destinatario es obligatorio")
        @Size(max = 100, message = "El destinatario no puede pasar de 100 caracteres")
        String destinatario,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^[0-9+()\\- ]{7,20}$", message = "El teléfono debe tener de 7 a 20 dígitos")
        String telefono,

        @NotBlank(message = "La calle es obligatoria")
        @Size(max = 150, message = "La calle no puede pasar de 150 caracteres")
        String calle,

        @NotBlank(message = "El número exterior es obligatorio")
        @Size(max = 20, message = "El número exterior no puede pasar de 20 caracteres")
        String numeroExterior,

        @NotBlank(message = "La colonia es obligatoria")
        @Size(max = 100, message = "La colonia no puede pasar de 100 caracteres")
        String colonia,

        @NotBlank(message = "La ciudad es obligatoria")
        @Size(max = 100, message = "La ciudad no puede pasar de 100 caracteres")
        String ciudad,

        @NotBlank(message = "El estado es obligatorio")
        @Size(max = 100, message = "El estado no puede pasar de 100 caracteres")
        String estado,

        @NotBlank(message = "El código postal es obligatorio")
        @Pattern(regexp = "^\\d{5}$", message = "El código postal debe tener 5 dígitos")
        String codigoPostal) {
}