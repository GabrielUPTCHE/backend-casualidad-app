package com.casualidad.casualidad_backend.pagos.dto.response;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import com.casualidad.casualidad_backend.common.domain.enums.MetodoPago;

public record DetalleAbonoReporteDto(
    Long idPago,
    LocalDateTime fechaPago,
    BigDecimal  monto,
    MetodoPago metodoPago,
    String codigoPedido,   // Para que el admin sepa de qué pedido es
    String nombreCliente
) {}