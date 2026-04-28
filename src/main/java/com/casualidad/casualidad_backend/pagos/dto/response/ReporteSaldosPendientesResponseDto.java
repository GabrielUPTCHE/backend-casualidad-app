package com.casualidad.casualidad_backend.pagos.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ReporteSaldosPendientesResponseDto(
    BigDecimal totalPorCobrar,
    Integer cantidadPedidosPendientes,
    List<DetalleSaldoPendienteDto> pedidos
) {}