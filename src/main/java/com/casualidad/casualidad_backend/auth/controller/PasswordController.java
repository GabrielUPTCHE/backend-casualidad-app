package com.casualidad.casualidad_backend.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.casualidad.casualidad_backend.auth.dto.CambiarPasswordDto;
import com.casualidad.casualidad_backend.auth.entity.Usuario;
import com.casualidad.casualidad_backend.auth.services.SeguridadPasswordService;
import com.casualidad.casualidad_backend.common.domain.dtos.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/perfil/seguridad/password")
@RequiredArgsConstructor
public class PasswordController {

    private final SeguridadPasswordService passwordService;

    @PostMapping("/solicitar-codigo")
    public ResponseEntity<ApiResponse<String>> solicitarCodigo(@AuthenticationPrincipal Usuario usuario) {
        passwordService.solicitarCodigo(usuario);
        return ResponseEntity.ok(ApiResponse.<String>builder().code(200).message("Código enviado.").build());
    }

    @PutMapping("/cambiar")
    public ResponseEntity<ApiResponse<String>> cambiarPassword(
            @Valid @RequestBody CambiarPasswordDto dto,
            @AuthenticationPrincipal Usuario usuario
    ) {
        passwordService.cambiarPassword(usuario, dto.codigo(), dto.nuevaPassword());
        return ResponseEntity.ok(ApiResponse.<String>builder().code(200).message("Contraseña actualizada.").build());
    }
}