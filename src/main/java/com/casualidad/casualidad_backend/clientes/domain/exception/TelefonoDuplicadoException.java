package com.casualidad.casualidad_backend.clientes.domain.exception;

public class TelefonoDuplicadoException extends RuntimeException {
    public TelefonoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
