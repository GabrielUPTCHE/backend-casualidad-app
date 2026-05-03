package com.casualidad.casualidad_backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ValidarCorreoActualDto(
        @NotBlank(message = "El código es obligatorio")
        String codigoActual,

        @NotBlank(message = "El nuevo correo es obligatorio")
        @Email(message = "Formato de correo inválido")
        String nuevoCorreo
) {}
