package com.casualidad.casualidad_backend.auth.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.casualidad.casualidad_backend.auth.dto.ActualizarAdministradorRequestDto;
import com.casualidad.casualidad_backend.auth.entity.Usuario;
import com.casualidad.casualidad_backend.auth.mapper.UsuarioMapper;
import com.casualidad.casualidad_backend.auth.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class ActualizarAdministradorUseCase {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    @Transactional
    public String ejecutar(Long idUsuario, ActualizarAdministradorRequestDto dto) {
        
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + idUsuario));

        usuarioMapper.actualizarEntidad(dto, usuario);

        usuarioRepository.save(usuario);

        return "Usuario modificado con éxito.";
    }
}
