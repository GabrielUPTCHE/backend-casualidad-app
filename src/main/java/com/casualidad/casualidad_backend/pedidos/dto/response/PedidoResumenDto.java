package com.casualidad.casualidad_backend.pedidos.dto.response;

import com.casualidad.casualidad_backend.common.domain.enums.EstadoPedido;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PedidoResumenDto(
    Long idPedido,
    String codigoUnico,
    String nombreCliente, // Concatenaremos nombre y apellido en el mapeo
    EstadoPedido estadoPedido,
    LocalDate fechaEntrega,
    BigDecimal total,
    BigDecimal saldoPendiente
) {}
