package com.casualidad.casualidad_backend.pagos.service;

import com.casualidad.casualidad_backend.common.domain.enums.MetodoPago;
import com.casualidad.casualidad_backend.pagos.domain.model.Pago;
import com.casualidad.casualidad_backend.pagos.dto.response.DetalleAbonoReporteDto;
import com.casualidad.casualidad_backend.pagos.dto.response.ReporteIngresosResponseDto;
import com.casualidad.casualidad_backend.pagos.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerarReporteIngresosUseCase {

    private final PagoRepository pagoRepository;

    @Transactional(readOnly = true)
    public ReporteIngresosResponseDto ejecutar(LocalDate fechaInicio, LocalDate fechaFin) {
        
        // CA: Validación de fechas (Escenario de error)
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        // Manejo de horas para cubrir el día completo
        LocalDateTime inicio = fechaInicio.atStartOfDay(); // 00:00:00
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX); // 23:59:59.999...

        List<Pago> pagos = pagoRepository.buscarIngresosPorPeriodo(inicio, fin);

        // Si no hay registros (CA 5), aunque la lista esté vacía, el DTO llevará totales en 0
        // El controller puede decidir si lanzar error o devolver el DTO vacío.
        
        // Calcular Totales
        BigDecimal totalEfectivo = pagos.stream()
                .filter(p -> p.getMetodoPago() == MetodoPago.EFECTIVO)
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTransferencia = pagos.stream()
                .filter(p -> p.getMetodoPago() == MetodoPago.TRANSFERENCIA)
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGeneral = totalEfectivo.add(totalTransferencia);

        // Mapear a DTO de detalle
        List<DetalleAbonoReporteDto> detalles = pagos.stream()
                .map(p -> new DetalleAbonoReporteDto(
                        p.getIdPago(),
                        p.getFechaPago(),
                        p.getMonto(),
                        p.getMetodoPago(),
                        p.getPedido().getCodigoUnico(),
                        p.getPedido().getCliente().getNombre() 
                )).toList();

        return new ReporteIngresosResponseDto(
                totalGeneral,
                totalEfectivo,
                totalTransferencia,
                pagos.size(),
                detalles
        );
    }
}
