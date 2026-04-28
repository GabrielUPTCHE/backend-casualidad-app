package com.casualidad.casualidad_backend.pagos.service;

import com.casualidad.casualidad_backend.pagos.dto.response.DetalleAbonoReporteDto;
import com.casualidad.casualidad_backend.pagos.dto.response.DetalleSaldoPendienteDto;
import com.casualidad.casualidad_backend.pagos.dto.response.ReporteIngresosResponseDto;
import com.casualidad.casualidad_backend.pagos.dto.response.ReporteSaldosPendientesResponseDto;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ReporteExcelService {

    public byte[] generarExcel(ReporteIngresosResponseDto datos) {
        // Usamos XSSFWorkbook para generar el formato .xlsx (CA 3)
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Reporte de Ingresos");

            // Crear estilo en negrita para los encabezados
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // --- SECCIÓN 1: RESUMEN FINANCIERO ---
            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("Resumen de Ingresos");
            titleRow.getCell(0).setCellStyle(headerStyle);

            sheet.createRow(1).createCell(0).setCellValue("Total General:");
            sheet.getRow(1).createCell(1).setCellValue(datos.totalGeneral().doubleValue());

            sheet.createRow(2).createCell(0).setCellValue("Total Efectivo:");
            sheet.getRow(2).createCell(1).setCellValue(datos.totalEfectivo().doubleValue());

            sheet.createRow(3).createCell(0).setCellValue("Total Transferencia:");
            sheet.getRow(3).createCell(1).setCellValue(datos.totalTransferencia().doubleValue());

            // --- SECCIÓN 2: ENCABEZADOS DEL DETALLE ---
            Row headerRow = sheet.createRow(5);
            String[] columns = {"ID Pago", "Fecha", "Monto", "Método", "Pedido", "Cliente"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // --- SECCIÓN 3: DATOS DEL DETALLE ---
            int rowIdx = 6;
            for (DetalleAbonoReporteDto detalle : datos.detalles()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(detalle.idPago());
                // Formateamos la fecha a un String legible
                row.createCell(1).setCellValue(detalle.fechaPago().toString()); 
                row.createCell(2).setCellValue(detalle.monto().doubleValue());
                row.createCell(3).setCellValue(detalle.metodoPago().name());
                row.createCell(4).setCellValue(detalle.codigoPedido());
                row.createCell(5).setCellValue(detalle.nombreCliente());
            }

            // Ajustar el ancho de las columnas automáticamente
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Escribir los datos en el flujo de salida
            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error inesperado al generar el archivo Excel", e);
        }
    }

    public byte[] generarExcelSaldos(ReporteSaldosPendientesResponseDto datos) {
    try (Workbook workbook = new XSSFWorkbook();
         ByteArrayOutputStream out = new ByteArrayOutputStream()) {

        Sheet sheet = workbook.createSheet("Saldos Pendientes");
        CellStyle headerStyle = createHeaderStyle(workbook); // Método privado para no repetir código

        // Resumen
        Row resRow = sheet.createRow(0);
        resRow.createCell(0).setCellValue("Total Proyectado por Cobrar:");
        resRow.createCell(1).setCellValue(datos.totalPorCobrar().doubleValue());

        // Cabeceras
        Row headerRow = sheet.createRow(2);
        String[] columns = {"Código Pedido", "Cliente", "Fecha Entrega", "Monto Total", "Saldo Pendiente"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Datos
        int rowIdx = 3;
        for (DetalleSaldoPendienteDto pedido : datos.pedidos()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(pedido.codigoPedido());
            row.createCell(1).setCellValue(pedido.nombreCliente());
            row.createCell(2).setCellValue(pedido.fechaEntrega().toString());
            row.createCell(3).setCellValue(pedido.montoTotal().doubleValue());
            row.createCell(4).setCellValue(pedido.saldoPendiente().doubleValue());
        }

        for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);

        workbook.write(out);
        return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar Excel de saldos", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}