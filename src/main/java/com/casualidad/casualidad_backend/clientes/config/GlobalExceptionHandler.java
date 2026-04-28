package com.casualidad.casualidad_backend.clientes.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.casualidad.casualidad_backend.clientes.domain.exception.TelefonoDuplicadoException;
import com.casualidad.casualidad_backend.common.domain.dtos.response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TelefonoDuplicadoException.class)
    public ResponseEntity<ApiResponse<Void>> handleTelefonoDuplicado(TelefonoDuplicadoException ex) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message(ex.getMessage())
                .code(HttpStatus.BAD_REQUEST.value())
                .data(null)
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Captura errores de validación (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldError().getDefaultMessage();
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message(mensaje)
                .code(HttpStatus.BAD_REQUEST.value())
                .data(null)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
