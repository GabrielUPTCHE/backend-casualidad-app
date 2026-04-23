package com.casualidad.casualidad_backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
 
public class AuthDTOs {
 
 
    public record LoginRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Formato de correo inválido")
        @Size(max = 40, message = "El correo no puede superar 40 caracteres")
        String correo,
 
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(max = 40, message = "La contraseña no puede superar 40 caracteres")
        String contraseña
    ) {}
 
    public record RefreshRequest(
        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken
    ) {}
 
 
    public record LoginResponse(
        String accessToken,
        String refreshToken,
        UsuarioInfo usuario
    ) {}
 
    public record RefreshResponse(
        String accessToken,
        String refreshToken
    ) {}
 
    public record UsuarioInfo(
        String id,
        String nombre,
        String email,
        String rol
    ) {}
}