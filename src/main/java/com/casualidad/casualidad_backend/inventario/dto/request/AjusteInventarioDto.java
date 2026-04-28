package com.casualidad.casualidad_backend.inventario.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AjusteInventarioDto(
    @NotNull(message = "El ID del producto es obligatorio")
    Long idProducto,

    @NotNull(message = "La nueva cantidad es obligatoria")
    @DecimalMin(value = "0.0", message = "La cantidad no puede ser negativa") // Escenario (-)
    BigDecimal cantidadNueva,

    @NotBlank(message = "El motivo del ajuste es obligatorio") // CA 1
    @Size(min = 10, max = 255, message = "El motivo debe ser más descriptivo (mínimo 10 caracteres)")
    String motivo
) {}
