package com.example.Studio21.exception;

// El recurso pedido no existe o no pertenece al usuario.
public class NotFoundException extends RuntimeException {
    public NotFoundException(String mensaje) {
        super(mensaje);
    }
}
