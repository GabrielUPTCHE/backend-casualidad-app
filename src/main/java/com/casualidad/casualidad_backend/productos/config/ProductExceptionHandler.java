package com.casualidad.casualidad_backend.productos.config;

import com.casualidad.casualidad_backend.common.domain.dtos.response.ApiResponse;
import com.casualidad.casualidad_backend.clientes.domain.exception.TelefonoDuplicadoException;
import com.casualidad.casualidad_backend.productos.domain.exception.YaExisteProductoException;
import com.casualidad.casualidad_backend.productos.domain.exception.ProductoDeReventaException;
import com.casualidad.casualidad_backend.productos.domain.exception.ProductoDeInsumoException;
import com.casualidad.casualidad_backend.productos.domain.exception.UnidadDeMedidaException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ProductExceptionHandler {

    /**
     * Manejador centralizado para todas las Reglas de Negocio (Custom Exceptions).
     * Agrupamos todas las excepciones específicas que devuelven un 400 Bad Request.
     */
    @ExceptionHandler({
            YaExisteProductoException.class,
            ProductoDeReventaException.class,
            ProductoDeInsumoException.class,
            UnidadDeMedidaException.class,
            TelefonoDuplicadoException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleReglasDeNegocio(RuntimeException ex) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message(ex.getMessage())
                .code(HttpStatus.BAD_REQUEST.value())
                .data(null)
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }



    /**
     * Manejador para validaciones de Spring (@Valid en los DTOs).
     * Devuelve los campos que fallaron y el por qué.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .message("Error en la validación de los datos enviados")
                .code(HttpStatus.BAD_REQUEST.value())
                .data(errores)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Manejador para errores de sintaxis en el JSON o Enums inválidos (Ej: enviar TIPO="OTRO").
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleJsonInvalido(HttpMessageNotReadableException ex) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Formato de datos incorrecto o valores no permitidos en la petición")
                .code(HttpStatus.BAD_REQUEST.value())
                .data(null)
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Catch-All (Cazador global) para cualquier error no controlado (Ej: Caída de Base de Datos).
     * Evita que Spring devuelva su traza de error por defecto que expone información sensible.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleExcepcionesGlobales(Exception ex) {
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Ocurrió un error interno en el servidor. Por favor, contacte al administrador.")
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .data(null)
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}