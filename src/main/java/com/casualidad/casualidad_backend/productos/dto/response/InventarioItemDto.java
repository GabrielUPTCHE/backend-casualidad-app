package com.casualidad.casualidad_backend.productos.dto.response;

import java.math.BigDecimal;

import com.casualidad.casualidad_backend.common.domain.enums.TipoProducto;

public record InventarioItemDto(
    Long idProducto,
    String nombre,
    TipoProducto tipo,
    String unidadMedida,
    BigDecimal cantidadDisponible,
    boolean stockBajo // CA 5: Indicador de stock por debajo del mínimo
) {}
