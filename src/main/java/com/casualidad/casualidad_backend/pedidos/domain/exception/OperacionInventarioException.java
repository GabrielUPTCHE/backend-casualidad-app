package com.casualidad.casualidad_backend.pedidos.domain.exception;

public class OperacionInventarioException extends RuntimeException {
    public OperacionInventarioException(String mensaje) {
        super(mensaje);
    }
}