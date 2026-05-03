package com.casualidad.casualidad_backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmarNuevoCorreoDto(
        @NotBlank(message = "El código es obligatorio")
        String codigoNuevo
) {}
