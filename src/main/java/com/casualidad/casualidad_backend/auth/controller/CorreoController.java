package com.casualidad.casualidad_backend.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.casualidad.casualidad_backend.auth.dto.ConfirmarNuevoCorreoDto;
import com.casualidad.casualidad_backend.auth.dto.ValidarCorreoActualDto;
import com.casualidad.casualidad_backend.auth.entity.Usuario;
import com.casualidad.casualidad_backend.auth.services.SeguridadCorreoService;
import com.casualidad.casualidad_backend.common.domain.dtos.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/perfil/seguridad/correo")
@RequiredArgsConstructor
public class CorreoController {

    private final SeguridadCorreoService correoService;

    @PostMapping("/solicitar-codigo-actual")
    public ResponseEntity<ApiResponse<String>> solicitarCodigoActual(@AuthenticationPrincipal Usuario usuario) {
        correoService.solicitarCodigoCorreoActual(usuario);
        return ResponseEntity.ok(ApiResponse.<String>builder().code(200).message("Código enviado.").build());
    }

    @PostMapping("/validar-actual-y-solicitar-nuevo")
    public ResponseEntity<ApiResponse<String>> procesarPasoIntermedio(
            @Valid @RequestBody ValidarCorreoActualDto dto,
            @AuthenticationPrincipal Usuario usuario
    ) {
        correoService.validarActualYSolicitarNuevo(usuario, dto.codigoActual(), dto.nuevoCorreo());
        return ResponseEntity.ok(ApiResponse.<String>builder().code(200).message("Código enviado al nuevo correo.").build());
    }

    @PutMapping("/confirmar-cambio")
    public ResponseEntity<ApiResponse<String>> confirmarCambio(
            @Valid @RequestBody ConfirmarNuevoCorreoDto dto,
            @AuthenticationPrincipal Usuario usuario
    ) {
        correoService.confirmarCambioDeCorreo(usuario, dto.codigoNuevo());
        return ResponseEntity.ok(ApiResponse.<String>builder().code(200).message("Correo actualizado exitosamente.").build());
    }
}