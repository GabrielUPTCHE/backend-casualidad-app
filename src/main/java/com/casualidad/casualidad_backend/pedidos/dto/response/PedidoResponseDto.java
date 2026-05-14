package com.casualidad.casualidad_backend.pedidos.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PedidoResponseDto(
        Long idPedido,
        String codigoUnico, 
        String estado,
        BigDecimal precioTotal,
        BigDecimal saldoPendiente,
        LocalDateTime fechaRegistro,
        LocalDate fechaEsperadaEntrega,
        List<DetallePedidoResponseDto> detalles
) {
    // Sub-DTO anidado exclusivo para la respuesta
    @Builder
    public record DetallePedidoResponseDto(
            Long idProducto,
            String nombreProducto,
            BigDecimal cantidad,
            BigDecimal precioUnitarioGrabado,
            BigDecimal subtotal
    ) {}
}