package com.casualidad.casualidad_backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ActualizarAdministradorRequestDto(

        @NotBlank(message = "Los nombres no pueden estar vacíos")
        @Size(max = 50, message = "Los nombres no pueden superar los 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "Los nombres no deben contener caracteres especiales ni números")
        String nombre,

        @NotBlank(message = "Los apellidos no pueden estar vacíos")
        @Size(max = 50, message = "Los apellidos no pueden superar los 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "Los apellidos no deben contener caracteres especiales ni números")
        String apellidos,

        @NotBlank(message = "El teléfono no puede estar vacío")
        @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe ser numérico y tener exactamente 10 dígitos")
        String telefono
) {}
