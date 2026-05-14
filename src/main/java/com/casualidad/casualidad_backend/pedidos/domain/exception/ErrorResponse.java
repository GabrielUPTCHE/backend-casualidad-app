package com.casualidad.casualidad_backend.pedidos.domain.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        String mensaje,
        String estado,
        LocalDateTime timestamp
) {}