package com.casualidad.casualidad_backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class RegisterRequest {
    private String nombre;
    private String correo;
    private String contraseña;
    private String rol; // "ADMIN"}
}