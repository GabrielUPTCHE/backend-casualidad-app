package com.casualidad.casualidad_backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ValidarCodigoRequestDto(
        @NotBlank(message = "El código es obligatorio")
        @Size(min = 6, max = 6, message = "El código debe tener exactamente 6 dígitos")
        @Pattern(regexp = "^[0-9]+$", message = "El código solo debe contener números")
        String codigo
) {}
