package com.casualidad.casualidad_backend.pagos.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.math.BigDecimal;

public record ReporteIngresosResponseDto(
    BigDecimal totalGeneral,
    BigDecimal totalEfectivo,
    BigDecimal totalTransferencia,
    Integer cantidadAbonos,
    List<DetalleAbonoReporteDto> detalles
) {}