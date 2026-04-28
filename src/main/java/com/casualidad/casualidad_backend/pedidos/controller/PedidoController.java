package com.casualidad.casualidad_backend.pedidos.controller;

import com.casualidad.casualidad_backend.common.domain.dtos.response.PageResponse;
import com.casualidad.casualidad_backend.common.domain.enums.EstadoPedido;
import com.casualidad.casualidad_backend.pedidos.dto.request.ActualizarPedidoDto;
import com.casualidad.casualidad_backend.pedidos.dto.request.CrearPedidoDto;
import com.casualidad.casualidad_backend.pedidos.dto.request.FiltroPedidoDto;
import com.casualidad.casualidad_backend.pedidos.dto.response.PedidoDetalleCompletoDto;
import com.casualidad.casualidad_backend.pedidos.dto.response.PedidoResponseDto;
import com.casualidad.casualidad_backend.pedidos.dto.response.PedidoResumenDto;
import com.casualidad.casualidad_backend.pedidos.service.ActivarProduccionPedidoUseCase;
import com.casualidad.casualidad_backend.pedidos.service.CancelarPedidoUseCase;
import com.casualidad.casualidad_backend.pedidos.service.CrearPedidoUseCase;
import com.casualidad.casualidad_backend.pedidos.service.EditarPedidoUseCase;
import com.casualidad.casualidad_backend.pedidos.service.ListarPedidosUseCase;
import com.casualidad.casualidad_backend.pedidos.service.ObtenerDetallePedidoUseCase;

import org.springframework.data.domain.Sort;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final CrearPedidoUseCase crearPedidoUseCase;
    private final ActivarProduccionPedidoUseCase activarProduccionPedidoUseCase;
    private final EditarPedidoUseCase editarPedidoUseCase;
    private final CancelarPedidoUseCase cancelarPedidoUseCase;
    private final ListarPedidosUseCase listarPedidosUseCase;
    private final ObtenerDetallePedidoUseCase obtenerDetallePedidoUseCase;
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
        String codigoUnico = activarProduccionPedidoUseCase.ejecutar(idPedido);
        
        return ResponseEntity.ok(Map.of(
            "mensaje", "Pedido pasado a producción exitosamente",
            "codigoUnico", codigoUnico,
            "estado", "EN_PRODUCCION"
        ));
    }

    @PutMapping("/{idPedido}")
    public ResponseEntity<Map<String, Object>> actualizarPedido(
            @PathVariable Long idPedido,
            @Valid @RequestBody ActualizarPedidoDto request) {
        
        editarPedidoUseCase.ejecutar(idPedido, request);
        
        return ResponseEntity.ok(Map.of(
                "estado", "EXITO",
                "mensaje", "Pedido actualizado correctamente según las reglas de su estado actual."
        ));
    }

    @PatchMapping("/{idPedido}/cancelar")
    public ResponseEntity<Map<String, Object>> cancelarPedido(
            @PathVariable Long idPedido,
            @RequestParam(defaultValue = "false") boolean reintegrarMateriales) {
        
        cancelarPedidoUseCase.ejecutar(idPedido, reintegrarMateriales);
        
        return ResponseEntity.ok(Map.of(
                "estado", "EXITO",
                "mensaje", "Pedido cancelado correctamente.",
                "materialesReintegrados", reintegrarMateriales
        ));
    }

    @GetMapping
    public ResponseEntity<PageResponse<PedidoResumenDto>> listarPedidos(
            @RequestParam(required = false) Long idCliente,
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        FiltroPedidoDto filtro = new FiltroPedidoDto(idCliente, estado, fechaInicio, fechaFin);
        
        // Ordenamos por defecto para que los pedidos con fecha de entrega más próxima salgan primero
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "fechaEntrega"));
        
        return ResponseEntity.ok(listarPedidosUseCase.ejecutar(filtro, pageRequest));
    }

    @GetMapping("/{idPedido}")
    public ResponseEntity<PedidoDetalleCompletoDto> obtenerDetallePedido(
            @PathVariable Long idPedido,
            @RequestParam(defaultValue = "0") int abonosPage,
            @RequestParam(defaultValue = "5") int abonosSize
    ) {
        // Paginación específica para la lista de abonos, ordenada por fecha descendente (más reciente primero)
        PageRequest pageRequest = PageRequest.of(abonosPage, abonosSize, Sort.by(Sort.Direction.DESC, "fechaPago"));
        
        return ResponseEntity.ok(obtenerDetallePedidoUseCase.ejecutar(idPedido, pageRequest));
    }

    
}