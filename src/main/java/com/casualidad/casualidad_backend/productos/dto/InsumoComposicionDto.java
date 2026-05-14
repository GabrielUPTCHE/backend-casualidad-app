package com.casualidad.casualidad_backend.productos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record InsumoComposicionDto(
    @NotNull(message = "El ID del insumo es obligatorio")
    Long idInsumo,

    @NotNull(message = "La cantidad usada es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a 0")
    BigDecimal cantidadUsada
) {}
