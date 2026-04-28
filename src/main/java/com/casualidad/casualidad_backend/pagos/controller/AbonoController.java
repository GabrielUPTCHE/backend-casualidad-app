package com.casualidad.casualidad_backend.pagos.controller;

import com.casualidad.casualidad_backend.auth.entity.Usuario; // Ajusta según tu paquete
import com.casualidad.casualidad_backend.pagos.dto.request.EditarAbonoRequestDto;
import com.casualidad.casualidad_backend.pagos.dto.request.RegistrarAbonoRequestDto;
import com.casualidad.casualidad_backend.pagos.dto.response.AbonoResponseDto;
import com.casualidad.casualidad_backend.pagos.dto.response.SaldoActualizadoResponseDto;
import com.casualidad.casualidad_backend.pagos.service.EditarAbonoUseCase;
import com.casualidad.casualidad_backend.pagos.service.EliminarAbonoUseCase;
import com.casualidad.casualidad_backend.pagos.service.RegistrarAbonoUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos/{idPedido}/abonos")
@RequiredArgsConstructor
public class AbonoController {

    private final RegistrarAbonoUseCase registrarAbonoUseCase;
    private final EditarAbonoUseCase editarAbonoUseCase;
    private final EliminarAbonoUseCase eliminarAbonoUseCase;

    @PostMapping
    public ResponseEntity<AbonoResponseDto> registrarAbono(
            @PathVariable Long idPedido,
            @Valid @RequestBody RegistrarAbonoRequestDto request,
            @AuthenticationPrincipal Usuario admin 
    ) {
        AbonoResponseDto response = registrarAbonoUseCase.ejecutar(idPedido, request, admin);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // <-- 2. Agregamos el endpoint de edición
    @PutMapping("/{idPago}")
    public ResponseEntity<AbonoResponseDto> editarAbono(
            @PathVariable Long idPedido, // Mantenemos la jerarquía de la URL REST
            @PathVariable Long idPago,
            @Valid @RequestBody EditarAbonoRequestDto request,
            @AuthenticationPrincipal Usuario admin
    ) {
        // En nuestro UseCase la lógica ya busca el pago por su ID y saca el pedido de ahí.
        AbonoResponseDto response = editarAbonoUseCase.ejecutar(idPago, request, admin);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{idPago}")
    public ResponseEntity<SaldoActualizadoResponseDto> eliminarAbono(
            @PathVariable Long idPedido,
            @PathVariable Long idPago,
            @AuthenticationPrincipal Usuario admin
    ) {
        SaldoActualizadoResponseDto response = eliminarAbonoUseCase.ejecutar(idPago, admin);
        return ResponseEntity.ok(response);
    }
}