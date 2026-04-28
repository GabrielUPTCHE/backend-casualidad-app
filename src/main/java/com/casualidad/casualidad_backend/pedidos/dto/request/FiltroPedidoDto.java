package com.casualidad.casualidad_backend.pedidos.dto.request;

import com.casualidad.casualidad_backend.common.domain.enums.EstadoPedido;
import java.time.LocalDate;

public record FiltroPedidoDto(
    Long idCliente,
    EstadoPedido estado,
    LocalDate fechaInicio,
    LocalDate fechaFin
) {}