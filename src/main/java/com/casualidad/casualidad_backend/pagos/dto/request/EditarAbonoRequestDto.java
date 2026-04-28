package com.casualidad.casualidad_backend.pagos.dto.request;

import com.casualidad.casualidad_backend.common.domain.enums.MetodoPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record EditarAbonoRequestDto(
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    BigDecimal monto,

    @NotNull(message = "El método de pago es obligatorio")
    MetodoPago metodoPago,

    String referenciaComprobante 
) {}