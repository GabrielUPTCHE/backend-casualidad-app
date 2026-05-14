package com.casualidad.casualidad_backend.clientes.dto.response;

import java.util.List;

public record ClienteResponseDto(
    Long idCliente,
    String nombre,
    String direccion,
    List<String> telefonos
) {}