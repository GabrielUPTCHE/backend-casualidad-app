package com.casualidad.casualidad_backend.pedidos.controller;

import com.casualidad.casualidad_backend.pedidos.dto.request.CrearPedidoDto;
import com.casualidad.casualidad_backend.pedidos.dto.response.PedidoResponseDto;
import com.casualidad.casualidad_backend.pedidos.service.ActivarProduccionPedidoUseCase;
import com.casualidad.casualidad_backend.pedidos.service.CrearPedidoUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final CrearPedidoUseCase crearPedidoUseCase;
    private final ActivarProduccionPedidoUseCase activarProduccionPedidoUseCase;

    /**
     * HU 18: Registra un pedido inicial en estado PENDIENTE_ABONO.
     * Endpoint: POST /api/v1/pedidos
     *
     * @param request Datos del cliente, usuario creador, fecha de entrega y productos.
     * @return DTO con los datos básicos del pedido creado.
     */
    @PostMapping
    public ResponseEntity<PedidoResponseDto> crearPedido(@Valid @RequestBody CrearPedidoDto request) {
        
        PedidoResponseDto respuesta = crearPedidoUseCase.ejecutar(request);
        
        // Retornamos 201 CREATED indicando que el recurso se guardó con éxito
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PostMapping("/{idPedido}/activar-produccion")
    public ResponseEntity<?> activarProduccion(@PathVariable Long idPedido) {
        // El Use Case retorna el código único generado
        String codigoUnico = activarProduccionPedidoUseCase.ejecutar(idPedido);
        
        return ResponseEntity.ok(Map.of(
            "mensaje", "Pedido pasado a producción exitosamente",
            "codigoUnico", codigoUnico,
            "estado", "EN_PRODUCCION"
        ));
    }

    
}