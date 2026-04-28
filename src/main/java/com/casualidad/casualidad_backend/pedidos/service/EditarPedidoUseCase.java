package com.casualidad.casualidad_backend.pedidos.service;

import com.casualidad.casualidad_backend.common.domain.enums.EstadoPedido;
import com.casualidad.casualidad_backend.inventario.service.GestionInventarioPedidoService;
import com.casualidad.casualidad_backend.pedidos.domain.model.Pedido;
import com.casualidad.casualidad_backend.pedidos.domain.model.ProductoPedido;
import com.casualidad.casualidad_backend.pedidos.dto.request.ActualizarDetalleDto;
import com.casualidad.casualidad_backend.pedidos.dto.request.ActualizarPedidoDto;
import com.casualidad.casualidad_backend.pedidos.repository.PedidoRepository;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EditarPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final GestionInventarioPedidoService inventarioService;

    @Transactional
    public void ejecutar(Long idPedido, ActualizarPedidoDto request) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        validarEstadoEditable(pedido.getEstadoPedido());

        // Actualizar datos comunes a cualquier estado
        if (request.fechaEntrega() != null) {
            pedido.setFechaEntrega(request.fechaEntrega());
        }

        // Ramificación de lógica según el estado
        if (pedido.getEstadoPedido() == EstadoPedido.PENDIENTE) {
            editarEdicionTotal(pedido, request);
        } else if (pedido.getEstadoPedido() == EstadoPedido.EN_PRODUCCION) {
            editarEdicionRestringida(pedido, request);
        }

        pedidoRepository.save(pedido);
        log.info("Pedido {} actualizado exitosamente en estado {}", idPedido, pedido.getEstadoPedido());
    }

    // ========================================================================
    // MÉTODOS DE VALIDACIÓN GENERAL
    // ========================================================================

    private void validarEstadoEditable(EstadoPedido estado) {
        // CA 4: Bloqueo de estados finales (Corregido el duplicado TERMINADO)
        if (estado == EstadoPedido.TERMINADO ||  estado == EstadoPedido.CANCELADO) {
            throw new IllegalStateException("El pedido está " + estado + " y no admite modificaciones para proteger el historial.");
        }
    }

    // ========================================================================
    // FLUJO 1: ESTADO PENDIENTE (CA 1 y CA 5)
    // ========================================================================

    private void editarEdicionTotal(Pedido pedido, ActualizarPedidoDto request) {
        BigDecimal abonoRealizado = pedido.getTotal().subtract(pedido.getSaldoPendiente());
        
        pedido.getDetalles().clear(); // Hibernate se encarga de borrar los huérfanos
        BigDecimal nuevoTotal = BigDecimal.ZERO;

        for (ActualizarDetalleDto dto : request.detalles()) {
            Producto producto = buscarProducto(dto.idProducto());
            BigDecimal precioVenta = BigDecimal.valueOf(producto.getPrecioVenta());
            BigDecimal subtotal = precioVenta.multiply(BigDecimal.valueOf(dto.cantidad()));

            ProductoPedido nuevoDetalle = construirDetalle(pedido, producto, dto, precioVenta, subtotal);
            pedido.getDetalles().add(nuevoDetalle);
            
            nuevoTotal = nuevoTotal.add(subtotal);
        }

        // CA 5: Recalcular saldos
        pedido.setTotal(nuevoTotal);
        BigDecimal nuevoSaldo = nuevoTotal.subtract(abonoRealizado).max(BigDecimal.ZERO);
        pedido.setSaldoPendiente(nuevoSaldo);
    }

    // ========================================================================
    // FLUJO 2: ESTADO EN PRODUCCIÓN (CA 2)
    // ========================================================================

    private void editarEdicionRestringida(Pedido pedido, ActualizarPedidoDto request) {
        Map<Long, ProductoPedido> detallesActuales = pedido.getDetalles().stream()
                .collect(Collectors.toMap(ProductoPedido::getIdDetalle, d -> d));

        BigDecimal costoAdicionalTotal = BigDecimal.ZERO;

        for (ActualizarDetalleDto dto : request.detalles()) {
            if (dto.idDetalle() != null) {
                costoAdicionalTotal = costoAdicionalTotal.add(actualizarDetalleExistente(detallesActuales, dto));
            } else {
                costoAdicionalTotal = costoAdicionalTotal.add(agregarDetalleNuevo(pedido, dto));
            }
        }

        // Si quedan elementos en el mapa, significa que el usuario intentó omitir/borrar un producto
        if (!detallesActuales.isEmpty()) {
            throw new IllegalStateException("No se pueden eliminar productos de un pedido que ya está en producción.");
        }

        if (costoAdicionalTotal.compareTo(BigDecimal.ZERO) > 0) {
            pedido.setTotal(pedido.getTotal().add(costoAdicionalTotal));
            pedido.setSaldoPendiente(pedido.getSaldoPendiente().add(costoAdicionalTotal));
        }
    }

    private BigDecimal actualizarDetalleExistente(Map<Long, ProductoPedido> detallesActuales, ActualizarDetalleDto dto) {
        ProductoPedido actual = detallesActuales.remove(dto.idDetalle());
        
        if (actual == null) {
            throw new IllegalArgumentException("El detalle con ID " + dto.idDetalle() + " no pertenece a este pedido.");
        }
        if (!actual.getProducto().getIdProducto().equals(dto.idProducto())) {
            throw new IllegalStateException("No se puede cambiar el tipo de producto de un pedido en producción.");
        }
        if (dto.cantidad() < actual.getCantidad()) {
            throw new IllegalStateException("No se pueden reducir cantidades de un pedido en producción. Cancele el pedido para reintegrar materiales.");
        }

        int cantidadAdicional = dto.cantidad() - actual.getCantidad();
        if (cantidadAdicional > 0) {
            inventarioService.descontarInventario(actual.getProducto(), cantidadAdicional);
            actual.setCantidad(dto.cantidad());
            actual.setSubtotal(actual.getPrecioUnitario().multiply(BigDecimal.valueOf(dto.cantidad())));
        }

        actual.setObservaciones(dto.observaciones());
        return actual.getPrecioUnitario().multiply(BigDecimal.valueOf(cantidadAdicional));
    }

    private BigDecimal agregarDetalleNuevo(Pedido pedido, ActualizarDetalleDto dto) {
        Producto producto = buscarProducto(dto.idProducto());
        inventarioService.descontarInventario(producto, dto.cantidad());

        BigDecimal precioVenta = BigDecimal.valueOf(producto.getPrecioVenta());
        BigDecimal subtotal = precioVenta.multiply(BigDecimal.valueOf(dto.cantidad()));

        ProductoPedido nuevoDetalle = construirDetalle(pedido, producto, dto, precioVenta, subtotal);
        pedido.getDetalles().add(nuevoDetalle);

        return subtotal;
    }

    // ========================================================================
    // MÉTODOS UTILITARIOS
    // ========================================================================

    private Producto buscarProducto(Long idProducto) {
        return productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + idProducto));
    }

    private ProductoPedido construirDetalle(Pedido pedido, Producto producto, ActualizarDetalleDto dto, 
                                            BigDecimal precioVenta, BigDecimal subtotal) {
        return ProductoPedido.builder()
                .producto(producto)
                .cantidad(dto.cantidad())
                .precioUnitario(precioVenta)
                .subtotal(subtotal)
                .observaciones(dto.observaciones())
                .pedido(pedido)
                .build();
    }
}