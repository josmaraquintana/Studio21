package com.example.Studio21.dto;

//Respuesta de registro y login: el token y los datos para pintar la cabecera/cuenta
public record AuthResponse(String token, String tipo, long expiraEnMs, CuentaResponse usuario) {
}
