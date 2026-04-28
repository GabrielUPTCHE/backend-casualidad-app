package com.casualidad.casualidad_backend.pedidos.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DetallePedidoRequestDto(
    @NotNull(message = "El producto es obligatorio") Long idProducto,
    @NotNull @Min(1) Integer cantidad,
    String observaciones
) {}