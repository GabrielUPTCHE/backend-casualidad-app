package com.casualidad.casualidad_backend.common.domain.event;

import java.math.BigDecimal;

import com.casualidad.casualidad_backend.common.domain.enums.TipoMovimiento;

public record InventarioActualizadoEvent(
    Long idProducto,
    BigDecimal cantidadModificada,
    BigDecimal nuevoStockTotal,
    TipoMovimiento tipo
) {}