package com.casualidad.casualidad_backend.productos.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record DefinirComposicionDto(
    @NotNull(message = "El producto es obligatorio") Long idProducto,
    
    // Escenario (-): Error - Composición Vacía
    @NotEmpty(message = "Un producto elaborado debe tener al menos un insumo asociado para calcular su costo y stock") 
    @Valid List<InsumoRequeridoDto> insumos
) {}


