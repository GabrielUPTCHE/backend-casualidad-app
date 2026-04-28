package com.casualidad.casualidad_backend.pedidos.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record ActualizarPedidoDto(
    @FutureOrPresent(message = "La fecha no puede ser en el pasado") 
    LocalDate fechaEntrega,
    
    @NotEmpty(message = "El pedido no puede quedar sin detalles") 
    @Valid List<ActualizarDetalleDto> detalles
) {}

