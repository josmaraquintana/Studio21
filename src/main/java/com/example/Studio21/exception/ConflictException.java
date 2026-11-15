package com.example.Studio21.exception;
/** La petición choca con el estado actual
 * (p. ej. correo ya registrado). Se convierte en un error de tipo 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String mensaje) {
        super(mensaje);
    }
}
