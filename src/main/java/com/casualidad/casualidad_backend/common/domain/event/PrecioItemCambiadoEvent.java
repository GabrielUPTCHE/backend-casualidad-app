package com.casualidad.casualidad_backend.common.domain.event;

import java.math.BigDecimal;

public record PrecioItemCambiadoEvent(
    Long idItem, 
    String nombreItem, 
    BigDecimal nuevoPrecio
) {}
