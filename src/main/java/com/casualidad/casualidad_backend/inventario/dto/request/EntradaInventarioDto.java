package com.casualidad.casualidad_backend.inventario.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

import com.casualidad.casualidad_backend.common.domain.enums.MotivoMovimiento;

public record EntradaInventarioDto(
    @NotNull(message = "El ID del producto es obligatorio")
    Long idProducto,

    // CA 1 y Escenario (-) Cantidad Inválida
    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero")
    BigDecimal cantidad,

    @NotNull(message = "El motivo de la entrada es obligatorio")
    MotivoMovimiento motivo
) {}