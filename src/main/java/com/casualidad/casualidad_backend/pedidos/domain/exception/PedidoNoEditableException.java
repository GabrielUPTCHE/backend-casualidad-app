package com.casualidad.casualidad_backend.pedidos.domain.exception;

public class PedidoNoEditableException extends RuntimeException {
    public PedidoNoEditableException(String mensaje) {
        super(mensaje);
    }
}