package com.casualidad.casualidad_backend.pagos.dto.response;
import java.time.LocalDate;
import java.math.BigDecimal;

public record DetalleSaldoPendienteDto(
    Long idPedido,
    String codigoPedido,
    String nombreCliente,
    LocalDate fechaEntrega,
    BigDecimal montoTotal,
    BigDecimal saldoPendiente
) {}
