package com.casualidad.casualidad_backend.pedidos.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.casualidad.casualidad_backend.pedidos.domain.exception.StockInsuficienteException;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.casualidad.casualidad_backend.pedidos.controller")
public class PedidoExceptionHandler {

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<?> handleStockInsuficiente(StockInsuficienteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "timestamp", LocalDateTime.now(),
            "estado", "ERROR_STOCK",
            "mensaje", "No hay insumos suficientes para iniciar la producción"
        ));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<?> handleBusinessExceptions(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "timestamp", LocalDateTime.now(),
            "estado", "ERROR_VALIDACION",
            "mensaje", ex.getMessage()
        ));
    }
}
