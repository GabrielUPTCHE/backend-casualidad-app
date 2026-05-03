
package com.casualidad.casualidad_backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambiarPasswordDto(
        @NotBlank(message = "El código es obligatorio")
        String codigo,

        @NotBlank(message = "La contraseña es obligatoria")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
                 message = "Mínimo 8 caracteres, 1 mayúscula, 1 número y 1 especial")
        String nuevaPassword
) {}
