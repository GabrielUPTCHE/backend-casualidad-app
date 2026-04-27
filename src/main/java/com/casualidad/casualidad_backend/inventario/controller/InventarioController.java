package com.casualidad.casualidad_backend.inventario.controller;

import com.casualidad.casualidad_backend.common.domain.dtos.response.ApiResponse;
import com.casualidad.casualidad_backend.inventario.dto.request.AjusteInventarioDto;
import com.casualidad.casualidad_backend.inventario.dto.request.EntradaInventarioDto;
import com.casualidad.casualidad_backend.inventario.service.AjustarInventarioUseCase;
import com.casualidad.casualidad_backend.inventario.service.RegistrarEntradaInventarioUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final RegistrarEntradaInventarioUseCase registrarEntradaUseCase;
    private final AjustarInventarioUseCase ajustarInventarioUseCase;

    @PostMapping("/entradas")
    public ResponseEntity<ApiResponse<Void>> registrarEntrada(
            @Valid @RequestBody EntradaInventarioDto dto) {
        
        registrarEntradaUseCase.ejecutar(dto);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Entrada de inventario registrada exitosamente")
                .code(HttpStatus.CREATED.value())
                .data(null)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/ajustes")
    public ResponseEntity<ApiResponse<String>> ajustarInventario(
            @Valid @RequestBody AjusteInventarioDto dto) {
        
        String mensaje = ajustarInventarioUseCase.ejecutar(dto);
        
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message(mensaje)
                .code(HttpStatus.OK.value())
                .data(null)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}