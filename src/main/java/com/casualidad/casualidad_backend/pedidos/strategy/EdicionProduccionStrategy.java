package com.casualidad.casualidad_backend.pedidos.strategy;

import com.casualidad.casualidad_backend.common.domain.enums.EstadoPedido;
import com.casualidad.casualidad_backend.inventario.service.GestionInventarioPedidoService;
import com.casualidad.casualidad_backend.pedidos.domain.model.Pedido;
import com.casualidad.casualidad_backend.pedidos.domain.model.ProductoPedido;
import com.casualidad.casualidad_backend.pedidos.dto.request.ActualizarDetalleDto;
import com.casualidad.casualidad_backend.pedidos.dto.request.ActualizarPedidoDto;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EdicionProduccionStrategy implements EdicionPedidoStrategy {

    private final ProductoRepository productoRepository;
    private final GestionInventarioPedidoService inventarioService; // Servicio auxiliar para descontar insumos

    @Override
    public EstadoPedido getEstadoSoportado() {
        return EstadoPedido.PENDIENTE;
    }

    @Override
    public void editar(Pedido pedido, ActualizarPedidoDto request) {
        if (request.fechaEntrega() != null) {
            pedido.setFechaEntrega(request.fechaEntrega());
        }

        Map<Long, ProductoPedido> actuales = pedido.getDetalles().stream()
                .collect(Collectors.toMap(ProductoPedido::getIdDetalle, d -> d));

        BigDecimal totalAdicional = BigDecimal.ZERO;

        for (ActualizarDetalleDto dto : request.detalles()) {
            if (dto.idDetalle() != null) {
                // Modificar producto existente
                ProductoPedido actual = actuales.get(dto.idDetalle());
                totalAdicional = totalAdicional.add(procesarEdicionExistente(actual, dto));
                actuales.remove(dto.idDetalle()); // Marcamos como procesado
            } else {
                // Agregar producto nuevo
                totalAdicional = totalAdicional.add(procesarProductoNuevo(pedido, dto));
            }
        }

        // Si quedaron elementos en el mapa, el frontend intentó omitir (eliminar) un producto
        if (!actuales.isEmpty()) {
            throw new IllegalStateException("No se pueden eliminar productos de un pedido que ya está en producción.");
        }

        // Si se agregó costo, actualizar total y saldo pendiente
        if (totalAdicional.compareTo(BigDecimal.ZERO) > 0) {
            pedido.setTotal(pedido.getTotal().add(totalAdicional));
            pedido.setSaldoPendiente(pedido.getSaldoPendiente().add(totalAdicional));
        }
    }

    private BigDecimal procesarEdicionExistente(ProductoPedido actual, ActualizarDetalleDto dto) {
        if (actual == null) throw new IllegalArgumentException("Detalle no pertenece al pedido.");
        
        if (!actual.getProducto().getIdProducto().equals(dto.idProducto())) {
            throw new IllegalStateException("No se puede cambiar el tipo de producto en producción.");
        }
        
        if (dto.cantidad() < actual.getCantidad()) {
            throw new IllegalStateException("No se pueden reducir cantidades de un pedido en producción. Cancele el pedido para reintegrar materiales.");
        }

        int diferenciaCantidad = dto.cantidad() - actual.getCantidad();
        
        if (diferenciaCantidad > 0) {
            // Descontar inventario SÓLO por la diferencia
            inventarioService.descontarInventario(actual.getProducto(), diferenciaCantidad);
            actual.setCantidad(dto.cantidad());
            actual.setSubtotal(actual.getPrecioUnitario().multiply(BigDecimal.valueOf(dto.cantidad())));
        }
        
        actual.setObservaciones(dto.observaciones());
        return actual.getPrecioUnitario().multiply(BigDecimal.valueOf(diferenciaCantidad)); // Dinero extra
    }

    private BigDecimal procesarProductoNuevo(Pedido pedido, ActualizarDetalleDto dto) {
        Producto producto = productoRepository.findById(dto.idProducto())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));

        // Descontar inventario completo para el nuevo producto
        inventarioService.descontarInventario(producto, dto.cantidad());

        BigDecimal precioVenta = BigDecimal.valueOf(producto.getPrecioVenta());
        BigDecimal subtotal = precioVenta.multiply(BigDecimal.valueOf(dto.cantidad()));

        ProductoPedido nuevoDetalle = ProductoPedido.builder()
                .producto(producto)
                .cantidad(dto.cantidad())
                .precioUnitario(precioVenta)
                .subtotal(subtotal)
                .observaciones(dto.observaciones())
                .pedido(pedido)
                .build();

        pedido.getDetalles().add(nuevoDetalle);
        return subtotal;
    }
}
