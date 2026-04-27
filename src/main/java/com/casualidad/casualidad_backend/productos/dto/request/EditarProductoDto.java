package com.casualidad.casualidad_backend.productos.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record EditarProductoDto(
    @NotBlank(message = "El nombre es obligatorio")
    @Pattern(regexp = "^[\\p{L}0-9\\s\\-\\'/]{3,50}$", message = "El nombre debe tener entre 3 y 50 caracteres y no permite caracteres especiales extraños.")
    String nombre,

    // Permitimos cambiar la unidad de medida
    Long idUnidadMedida,
    String nuevaUnidadMedida,

    @DecimalMin(value = "0.0", message = "El stock mínimo no puede ser negativo")
    @DecimalMax(value = "99999.99", message = "El stock mínimo excede el límite permitido")
    BigDecimal stockMinimo,

    @Min(value = 1, message = "El precio de compra debe ser mayor a 0")
    Integer precioCompra,

    @Min(value = 1, message = "El precio de venta debe ser mayor a 0")
    Integer precioVenta,

    @DecimalMin(value = "0.0", message = "El porcentaje sobrante no puede ser negativo")
    @DecimalMax(value = "100.0", message = "El porcentaje sobrante no puede superar 100")
    BigDecimal porcentajeSobrante
) {}
