package com.casualidad.casualidad_backend.pedidos.dto.response;

import com.casualidad.casualidad_backend.common.domain.enums.EstadoPedido;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record EditarPedidoResponse(
    Long idPedido,
    String codigoUnico,
    EstadoPedido estadoPedido,
    LocalDate fechaEntrega,
    BigDecimal total,
    BigDecimal saldoPendiente,
    LocalDateTime modificadoEn,
    boolean materialesDevueltos,
    List<DetallePedidoResponse> detalles
) {}