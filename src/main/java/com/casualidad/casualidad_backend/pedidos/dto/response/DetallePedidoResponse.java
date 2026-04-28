package com.casualidad.casualidad_backend.pedidos.dto.response;

import java.math.BigDecimal;

public record DetallePedidoResponse(
    Long idProductoPedido,
    Long idProducto,
    String nombreProducto,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal,
    String detallesPersonalizacion
) {}
