package com.casualidad.casualidad_backend.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.casualidad.casualidad_backend.auth.dto.ActualizarAdministradorRequestDto;
import com.casualidad.casualidad_backend.auth.entity.Usuario;
import com.casualidad.casualidad_backend.auth.services.ActualizarAdministradorUseCase;
import com.casualidad.casualidad_backend.common.domain.dtos.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final ActualizarAdministradorUseCase actualizarAdministradorUseCase;

    @PutMapping("/perfil")
    public ResponseEntity<ApiResponse<String>> actualizarPerfil(

            @AuthenticationPrincipal Usuario usuarioAutenticado, 
            // @Valid activa las validaciones del DTO (CA 1 y CA 2)
            @Valid @RequestBody ActualizarAdministradorRequestDto dto 
    ) {
        
        String mensaje = actualizarAdministradorUseCase.ejecutar(usuarioAutenticado.getId_usuario(), dto);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message(mensaje)
                .code(HttpStatus.OK.value())
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }
}
