package com.casualidad.casualidad_backend.inventario.scheduled;

import com.casualidad.casualidad_backend.auth.services.EmailService;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j // Para imprimir logs en la consola
public class AlertaInventarioTask {

    private final ProductoRepository productoRepository;
    private final EmailService emailService;

    // cron = "0 0 8 * * *" -> Todos los días a las 08:00:00 AM
    // Puedes cambiarlo a "0 0 8 * * MON-FRI" para que solo sea de lunes a viernes
    @Scheduled(cron = "0 0 8 * * *")
   /*  @Scheduled(initialDelay = 5000, fixedDelay = 600000) */
    @Transactional(readOnly = true)
    public void verificarYEnviarAlertasDeStock() {
        log.info("Iniciando revisión programada de stock bajo...");

        List<Producto> productosEnAlerta = productoRepository.findProductosConStockBajo();

        if (productosEnAlerta.isEmpty()) {
            log.info("Todos los productos tienen stock suficiente. No se envía alerta.");
            return;
        }

        String mensaje = construirMensajeCorreo(productosEnAlerta);
        
        // ¡RECUERDA CAMBIAR ESTE CORREO POR EL DEL ADMINISTRADOR!
        String correoAdmin = "casualidadtecnologia@gmail.com"; 
        
        emailService.enviarCorreo(
                correoAdmin, 
                "⚠️ Alerta Diaria: Productos con Stock Bajo", 
                mensaje
        );
        
        log.info("Alerta enviada exitosamente para {} productos.", productosEnAlerta.size());
    }

    private String construirMensajeCorreo(List<Producto> productos) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hola Administrador,\n\n");
        sb.append("El sistema ha detectado que los siguientes productos han alcanzado o bajado de su umbral mínimo de stock:\n\n");
        
        // Formato de tabla simple en texto plano (Alineado)
        sb.append(String.format("%-30s | %-12s | %-12s\n", "Producto", "Stock Actual", "Stock Mínimo"));
        sb.append("-".repeat(60)).append("\n");
        
        for (Producto p : productos) {
            // Usamos .toPlainString() para evitar notación científica si los BigDecimal son muy grandes o pequeños
            sb.append(String.format("%-30s | %-12s | %-12s\n", 
                    p.getNombre(), 
                    p.getCantidad().toPlainString(), 
                    p.getStockMinimo().toPlainString()));
        }
        
        sb.append("\nPor favor, gestione el reabastecimiento lo antes posible para no detener la operación.\n\n");
        sb.append("Atentamente,\nSistema Casualidad - Módulo de Inventario");
        
        return sb.toString();
    }
}