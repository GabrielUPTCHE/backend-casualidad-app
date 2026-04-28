package com.casualidad.casualidad_backend.inventario.service;

import com.casualidad.casualidad_backend.common.domain.enums.MotivoMovimiento;
import com.casualidad.casualidad_backend.common.domain.enums.TipoMovimiento;
import com.casualidad.casualidad_backend.inventario.domain.model.MovimientoInventario;
import com.casualidad.casualidad_backend.inventario.repository.MovimientoInventarioRepository;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GestionInventarioPedidoService {

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    /**
     * Propagation.MANDATORY asegura que este método falle si no es llamado
     * desde un Caso de Uso que ya tenga un @Transactional abierto.
     * Esto protege la base de datos de inconsistencias.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void descontarInventario(Producto productoVendido, Integer cantidad) {
        log.info("Descontando inventario para producto: {} (Cantidad: {})", productoVendido.getNombre(), cantidad);
        ejecutarMovimiento(productoVendido, BigDecimal.valueOf(cantidad), TipoMovimiento.SALIDA, MotivoMovimiento.CONSUMO);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void reintegrarInventario(Producto productoVendido, Integer cantidad) {
        log.info("Reintegrando inventario para producto: {} (Cantidad: {})", productoVendido.getNombre(), cantidad);
        // NOTA: Ajusta el MotivoMovimiento según los enums que tengas definidos (ej. CANCELACION, REINTEGRO, etc.)
        ejecutarMovimiento(productoVendido, BigDecimal.valueOf(cantidad), TipoMovimiento.ENTRADA, MotivoMovimiento.AJUSTE_INVENTARIO);
    }

    private void ejecutarMovimiento(Producto productoVendido, BigDecimal cantidadPedido, TipoMovimiento tipo, MotivoMovimiento motivo) {
        List<Producto> productosAActualizar = new ArrayList<>();
        List<MovimientoInventario> movimientos = new ArrayList<>();

        if (productoVendido.getComposicion() != null && !productoVendido.getComposicion().isEmpty()) {
            // Caso 1: Es un producto con receta (ej. Arreglo Floral). Se afectan sus insumos.
            productoVendido.getComposicion().forEach(receta -> {
                Producto insumo = receta.getInsumo();
                BigDecimal cantidadTotalInsumo = receta.getCantidadUsada().multiply(cantidadPedido);
                registrarCambio(insumo, cantidadTotalInsumo, tipo, motivo, productosAActualizar, movimientos);
            });
        } else {
            // Caso 2: Es un producto simple. Se afecta el producto directamente.
            registrarCambio(productoVendido, cantidadPedido, tipo, motivo, productosAActualizar, movimientos);
        }

        // Operaciones por lotes para optimizar rendimiento
        productoRepository.saveAll(productosAActualizar);
        movimientoRepository.saveAll(movimientos);
    }

    private void registrarCambio(Producto producto, BigDecimal cantidad, TipoMovimiento tipo, MotivoMovimiento motivo, 
                                 List<Producto> productosAActualizar, List<MovimientoInventario> movimientos) {
        
        BigDecimal stockActual = producto.getCantidad() != null ? producto.getCantidad() : BigDecimal.ZERO;
        BigDecimal nuevoStock;

        if (tipo == TipoMovimiento.SALIDA) {
            if (stockActual.compareTo(cantidad) < 0) {
                throw new IllegalStateException(String.format(
                        "Stock insuficiente para '%s'. Faltan %s unidades.",
                        producto.getNombre(), cantidad.subtract(stockActual)));
            }
            nuevoStock = stockActual.subtract(cantidad);
        } else { // ENTRADA
            nuevoStock = stockActual.add(cantidad);
        }

        // 1. Actualizar el stock en el Producto
        producto.setCantidad(nuevoStock);
        productosAActualizar.add(producto);

        // 2. Crear el registro histórico (MovimientoInventario)
        MovimientoInventario movimiento = MovimientoInventario.builder()
                .producto(producto)
                .cantidad(tipo == TipoMovimiento.SALIDA ? cantidad.negate() : cantidad)
                .cantidadAnterior(stockActual)
                .cantidadNueva(nuevoStock)
                .tipoMovimiento(tipo)
                .motivo(motivo)
                .comentario("Modificación de pedido (Edición/Cancelación)")
                .fechaMovimiento(LocalDateTime.now())
                .build();

        movimientos.add(movimiento);
    }
}
