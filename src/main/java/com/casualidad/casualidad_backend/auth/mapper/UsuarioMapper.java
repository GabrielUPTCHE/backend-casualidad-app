package com.casualidad.casualidad_backend.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.casualidad.casualidad_backend.auth.dto.ActualizarAdministradorRequestDto;
import com.casualidad.casualidad_backend.auth.entity.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    void actualizarEntidad(ActualizarAdministradorRequestDto dto, @MappingTarget Usuario usuario);
}
