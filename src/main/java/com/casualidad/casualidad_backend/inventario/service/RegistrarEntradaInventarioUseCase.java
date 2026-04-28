package com.casualidad.casualidad_backend.inventario.service;

import com.casualidad.casualidad_backend.common.domain.enums.TipoMovimiento;
import com.casualidad.casualidad_backend.common.domain.event.InventarioActualizadoEvent;
import com.casualidad.casualidad_backend.inventario.domain.exception.ProductoNoExisteException;
import com.casualidad.casualidad_backend.inventario.domain.model.MovimientoInventario;
import com.casualidad.casualidad_backend.inventario.dto.request.EntradaInventarioDto;
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
public class RegistrarEntradaInventarioUseCase {

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ApplicationEventPublisher eventPublisher; // Orquestador de Eventos

    @Transactional // CA 5: Transaccionalidad estricta
    public void ejecutar(EntradaInventarioDto dto) {
        
        // CA 3 y Escenario (-): Producto inexistente
        Producto producto = productoRepository.findById(dto.idProducto())
                .orElseThrow(ProductoNoExisteException::new);

        // CA 2: Incrementar el stock disponible
        BigDecimal nuevoStock = producto.getCantidad().add(dto.cantidad());
        producto.setCantidad(nuevoStock);
        productoRepository.save(producto);

        // CA 4: Registrar el movimiento histórico
        MovimientoInventario movimiento = MovimientoInventario.builder()
                .producto(producto)
                .cantidad(dto.cantidad())
                .tipoMovimiento(TipoMovimiento.ENTRADA) // Forzado a ENTRADA
                .motivo(dto.motivo())
                .fechaMovimiento(LocalDateTime.now()) // Fecha automática
                .build();
        
        movimientoRepository.save(movimiento);

        // Disparamos el evento para que otros módulos reaccionen si lo necesitan
        eventPublisher.publishEvent(new InventarioActualizadoEvent(
                producto.getIdProducto(), 
                dto.cantidad(), 
                nuevoStock, 
                TipoMovimiento.ENTRADA
        ));
    }
}
