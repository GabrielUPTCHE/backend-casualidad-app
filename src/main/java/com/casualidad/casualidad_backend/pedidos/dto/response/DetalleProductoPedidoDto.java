package com.casualidad.casualidad_backend.pedidos.dto.response;
import java.math.BigDecimal;

public record DetalleProductoPedidoDto(
    Long idDetalle,
    String nombreProducto,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal,
    String observaciones
) {}