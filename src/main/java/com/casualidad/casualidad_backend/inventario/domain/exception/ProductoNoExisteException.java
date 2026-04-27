package com.casualidad.casualidad_backend.inventario.domain.exception;

public class ProductoNoExisteException extends RuntimeException {
    public ProductoNoExisteException() {
        super("El producto no existe. Debe registrarlo previamente."); 
    }
}
