package com.casualidad.casualidad_backend.pagos.dto.response;

import com.casualidad.casualidad_backend.common.domain.enums.MetodoPago;
import com.casualidad.casualidad_backend.common.domain.enums.TipoPago;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AbonoResponseDto(
    Long idPago,
    BigDecimal monto,
    MetodoPago metodoPago,
    TipoPago tipoPago,
    LocalDateTime fechaPago,
    String referenciaComprobante,
    BigDecimal nuevoSaldoPendientePedido
) {}
