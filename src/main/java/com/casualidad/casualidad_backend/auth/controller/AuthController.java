package com.casualidad.casualidad_backend.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.casualidad.casualidad_backend.auth.dto.AuthDTOs.LoginRequest;
import com.casualidad.casualidad_backend.auth.dto.AuthDTOs.LoginResponse;
import com.casualidad.casualidad_backend.auth.dto.RegisterRequest;
import com.casualidad.casualidad_backend.auth.dto.RegisterResponse;
import com.casualidad.casualidad_backend.auth.security.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/recuperar-password")
    public ResponseEntity<String> recuperarPassword(@RequestParam String correo) {
        return ResponseEntity.ok("Si el correo existe, se ha enviado un enlace de recuperación");
    }

    @PostMapping("/recuperar-correo")
    public ResponseEntity<String> recuperarCorreo(@RequestParam String id_usuario) {
        return ResponseEntity.ok("Proceso de recuperación de correo iniciado");
    }

    @PostMapping("/registro")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.ok("Sesión cerrada correctamente");
    }
}