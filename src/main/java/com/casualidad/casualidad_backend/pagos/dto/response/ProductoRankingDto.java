package com.casualidad.casualidad_backend.pagos.dto.response;

import java.math.BigDecimal;

public record ProductoRankingDto(
    String nombreProducto,
    Long cantidadVendida,
    BigDecimal ingresosGenerados,
    String categoria
) {}