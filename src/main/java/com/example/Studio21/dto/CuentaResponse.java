package com.example.Studio21.dto;

import com.example.Studio21.model.Usuario;

/**
 *  Lo que la pantalla "Mi cuenta" muestra. Nunca incluye el hash de la contraseña.
 *
 * */
public record CuentaResponse(Long id, String nombre, String correo, String telefono, String rol) {

    public static CuentaResponse de(Usuario u) {
        return new CuentaResponse(u.getId(), u.getNombre(), u.getCorreo(), u.getTelefono(), u.getRol().name());
    }
}
