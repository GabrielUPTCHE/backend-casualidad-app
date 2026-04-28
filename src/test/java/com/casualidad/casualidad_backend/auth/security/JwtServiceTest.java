package com.casualidad.casualidad_backend.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.casualidad.casualidad_backend.auth.entity.Rol;
import com.casualidad.casualidad_backend.auth.entity.Usuario;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        String secret = Base64.getEncoder().encodeToString("casualidad-test-secret-key-2026-32bytes".getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(jwtService, "secretKey", secret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86_400_000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604_800_000L);
    }

    @Test
    void generateAndValidateToken() {
        Usuario usuario = usuario("admin@casualidad.com", "ADMIN");

        String token = jwtService.generateToken(usuario);
        String refreshToken = jwtService.generateRefreshToken(usuario);

        assertEquals("admin@casualidad.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, usuario));
        assertEquals("admin@casualidad.com", jwtService.extractUsername(refreshToken));
    }

    private Usuario usuario(String correo, String rol) {
        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        usuario.setActivo(true);
        usuario.setRol(new Rol(1L, rol, "ALTO", true));
        return usuario;
    }
}