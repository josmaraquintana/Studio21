package com.example.Studio21.dto;

public record DireccionDTO(
        Long id,
        String destinatario,
        String telefono,
        String calle,
        String numeroExterior,
        String colonia,
        String ciudad,
        String estado,
        String codigoPostal) {
}