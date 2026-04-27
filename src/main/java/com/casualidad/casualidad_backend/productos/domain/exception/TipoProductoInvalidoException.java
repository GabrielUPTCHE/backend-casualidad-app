package com.casualidad.casualidad_backend.productos.domain.exception;

public class TipoProductoInvalidoException extends RuntimeException {
    public TipoProductoInvalidoException(String mensaje) { super(mensaje); }
}
