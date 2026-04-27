package com.casualidad.casualidad_backend.productos.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder
public record ComposicionResponseDto(
    Long idProductoPadre,
    String nombreProducto,
    BigDecimal costoTotalProduccion, 
    List<DetalleInsumoDto> insumos
) {
    @Builder
    public record DetalleInsumoDto(
        Long idInsumo,
        String nombre,
        BigDecimal cantidadUsada,
        Integer precioUnitarioCompra,
        BigDecimal subtotal
    ) {}
}