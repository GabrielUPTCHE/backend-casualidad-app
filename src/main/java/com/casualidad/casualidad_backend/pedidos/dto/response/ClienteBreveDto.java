package com.casualidad.casualidad_backend.pedidos.dto.response;

public record ClienteBreveDto(
    Long idCliente,
    String nombreCompleto,
    String telefono
) {}
