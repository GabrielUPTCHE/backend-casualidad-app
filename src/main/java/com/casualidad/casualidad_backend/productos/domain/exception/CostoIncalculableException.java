package com.casualidad.casualidad_backend.productos.domain.exception;

public class CostoIncalculableException extends RuntimeException {
    public CostoIncalculableException(String insumo) {
        super("No se puede calcular el costo total porque faltan precios en el insumo asociado: " + insumo);
    }
}
