package com.casualidad.casualidad_backend.clientes.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record ClienteRequestDto(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50)
    String nombre,

    @NotEmpty(message = "Debe proporcionar al menos un número de teléfono")
    List<@NotBlank(message = "El teléfono no puede estar vacío") @Size(max = 20) String> telefonos,

    @Size(max = 100)
    String direccion
) {}
