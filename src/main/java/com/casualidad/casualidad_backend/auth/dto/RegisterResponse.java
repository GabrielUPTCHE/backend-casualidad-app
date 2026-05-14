package com.casualidad.casualidad_backend.auth.dto;

public record RegisterResponse(
    String accessToken,
    String refreshToken,
    String nombreUsuario
) {

}
