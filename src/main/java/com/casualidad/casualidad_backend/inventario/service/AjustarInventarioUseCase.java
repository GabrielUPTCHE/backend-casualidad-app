package com.casualidad.casualidad_backend.inventario.service;

import com.casualidad.casualidad_backend.common.domain.enums.MotivoMovimiento;
import com.casualidad.casualidad_backend.common.domain.enums.TipoMovimiento;
import com.casualidad.casualidad_backend.common.domain.event.InventarioActualizadoEvent;
import com.casualidad.casualidad_backend.inventario.domain.exception.ProductoNoExisteException;
import com.casualidad.casualidad_backend.inventario.domain.model.MovimientoInventario;
import com.casualidad.casualidad_backend.inventario.dto.request.AjusteInventarioDto;
import com.casualidad.casualidad_backend.inventario.repository.MovimientoInventarioRepository;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AjustarInventarioUseCase {

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public String ejecutar(AjusteInventarioDto dto) {
        Producto producto = productoRepository.findById(dto.idProducto())
                .orElseThrow(ProductoNoExisteException::new);

        BigDecimal cantidadAnterior = producto.getCantidad();
        BigDecimal cantidadNueva = dto.cantidadNueva();
        // El delta es lo que realmente cambió (puede ser negativo si hay merma)
        BigDecimal delta = cantidadNueva.subtract(cantidadAnterior);

        // 1. Actualizar el stock del producto
        producto.setCantidad(cantidadNueva);
        productoRepository.save(producto);

        // 2. Guardar registro histórico de auditoría (CA 2)
        MovimientoInventario movimiento = MovimientoInventario.builder()
                .producto(producto)
                .cantidad(delta)
                .cantidadAnterior(cantidadAnterior)
                .cantidadNueva(cantidadNueva)
                .tipoMovimiento(TipoMovimiento.AJUSTE)
                .motivo(MotivoMovimiento.AJUSTE_INVENTARIO)
                .comentario(dto.motivo())
                .fechaMovimiento(LocalDateTime.now())
                .build();
        
        movimientoRepository.save(movimiento);

        // 3. Notificar vía evento
        eventPublisher.publishEvent(new InventarioActualizadoEvent(
                producto.getIdProducto(), delta, cantidadNueva, TipoMovimiento.AJUSTE));

        // CA 3: Alerta de stock agotado
        if (cantidadNueva.compareTo(BigDecimal.ZERO) == 0) {
            return "Ajuste realizado. ¡ALERTA! El stock de '" + producto.getNombre() + "' se ha agotado.";
        }

        return "Ajuste de inventario procesado correctamente.";
    }
}
