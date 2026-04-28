package com.casualidad.casualidad_backend.pagos.dto.response;

import java.math.BigDecimal;

public record SaldoActualizadoResponseDto(
    BigDecimal nuevoSaldoPendientePedido,
    String mensaje
) {}
