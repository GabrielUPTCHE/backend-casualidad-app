package com.casualidad.casualidad_backend.pedidos.dto.response;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public record AbonoDto(
    Long idPago,
    BigDecimal monto,
    LocalDateTime fechaPago,
    String metodoPago
) {}
