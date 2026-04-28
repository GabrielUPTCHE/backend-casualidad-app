package com.casualidad.casualidad_backend.productos.domain.exception;

public class ComposicionVaciaException extends RuntimeException {
    public ComposicionVaciaException() {
        super("Un producto elaborado debe tener al menos un insumo asociado para calcular su costo y stock.");
    }
}