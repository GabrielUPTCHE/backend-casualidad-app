package com.casualidad.casualidad_backend.productos.dto.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
public record InsumoRequeridoDto(
    @NotNull(message = "El ID del insumo es obligatorio") Long idItem,
    
    @NotNull(message = "La cantidad requerida es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a 0") 
    BigDecimal cantidadRequerida
) {}
