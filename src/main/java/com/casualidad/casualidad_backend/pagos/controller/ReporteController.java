package com.casualidad.casualidad_backend.pagos.controller;

import com.casualidad.casualidad_backend.pagos.dto.response.ReporteIngresosResponseDto;
import com.casualidad.casualidad_backend.pagos.dto.response.ReporteSaldosPendientesResponseDto;
import com.casualidad.casualidad_backend.pagos.service.GenerarReporteIngresosUseCase;
import com.casualidad.casualidad_backend.pagos.service.GenerarReporteSaldosUseCase;
import com.casualidad.casualidad_backend.pagos.service.ReporteExcelService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final GenerarReporteIngresosUseCase generarReporteIngresosUseCase;
    private final ReporteExcelService reporteExcelService;
    private final GenerarReporteSaldosUseCase generarReporteSaldosUseCase;

    @GetMapping("/ingresos")
    public ResponseEntity<ReporteIngresosResponseDto> obtenerReporteIngresos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        // El UseCase ya maneja la lógica de expandir a LocalDateTime y las validaciones
        ReporteIngresosResponseDto reporte = generarReporteIngresosUseCase.ejecutar(fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }
    @GetMapping("/ingresos/exportar")
    public ResponseEntity<byte[]> exportarReporteIngresos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        // 1. Obtenemos los datos reutilizando el UseCase existente
        ReporteIngresosResponseDto datos = generarReporteIngresosUseCase.ejecutar(fechaInicio, fechaFin);

        // 2. Generamos el arreglo de bytes que representa el Excel
        byte[] excelContent = reporteExcelService.generarExcel(datos);

        // 3. Configuramos las cabeceras para forzar la descarga del archivo .xlsx
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "reporte_ingresos.xlsx");

        // 4. Retornamos el archivo
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelContent);
    }

    @GetMapping("/saldos-pendientes")
    public ResponseEntity<ReporteSaldosPendientesResponseDto> obtenerSaldosPendientes() {
        return ResponseEntity.ok(generarReporteSaldosUseCase.ejecutar());
    }

    @GetMapping("/saldos-pendientes/exportar")
    public ResponseEntity<byte[]> exportarSaldosPendientes() {
        ReporteSaldosPendientesResponseDto datos = generarReporteSaldosUseCase.ejecutar();
        byte[] excelContent = reporteExcelService.generarExcelSaldos(datos);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "saldos_pendientes.xlsx");

        return ResponseEntity.ok().headers(headers).body(excelContent);
    }
}
