package com.casualidad.casualidad_backend.pedidos.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record CrearPedidoDto(
    @NotNull(message = "El cliente es obligatorio") Long idCliente,
    @NotNull(message = "El usuario creador es obligatorio") Long idUsuario,
    @NotNull(message = "La fecha de entrega es obligatoria") 
    @FutureOrPresent LocalDate fechaEntrega,
    @NotEmpty(message = "Debe haber al menos un producto") 
    @Valid List<DetallePedidoRequestDto> detalles
) {}




