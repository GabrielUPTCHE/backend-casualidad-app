package com.casualidad.casualidad_backend.clientes.dto.response;

import lombok.Builder;
import java.util.List;

@Builder
public record ClienteListadoDto(
    Long idCliente,
    String nombre,
    String direccion,
    List<String> telefonos
) {}
