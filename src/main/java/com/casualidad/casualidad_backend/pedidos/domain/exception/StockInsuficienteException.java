package com.casualidad.casualidad_backend.pedidos.domain.exception;

import lombok.Getter;
import java.util.List;

@Getter
public class StockInsuficienteException extends RuntimeException {
    private final List<String> detallesFaltantes;

    public StockInsuficienteException(List<String> detallesFaltantes) {
        super("No se puede iniciar producción por falta de insumos.");
        this.detallesFaltantes = detallesFaltantes;
    }
}