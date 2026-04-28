package com.casualidad.casualidad_backend.pedidos.dto.response;

import com.casualidad.casualidad_backend.common.domain.dtos.response.PageResponse;
import com.casualidad.casualidad_backend.common.domain.enums.EstadoPedido;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// 1. DTO Principal que agrupa todo
public record PedidoDetalleCompletoDto(
    Long idPedido,
    String codigoUnico,
    EstadoPedido estadoPedido,
    LocalDate fechaEntrega,
    BigDecimal total,
    BigDecimal saldoPendiente,
    ClienteBreveDto cliente,
    List<DetalleProductoPedidoDto> productos,
    PageResponse<AbonoDto> historialAbonos
) {}