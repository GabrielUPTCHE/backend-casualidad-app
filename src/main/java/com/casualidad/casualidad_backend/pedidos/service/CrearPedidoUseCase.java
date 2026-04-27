package com.casualidad.casualidad_backend.pedidos.service;

import com.casualidad.casualidad_backend.clientes.domain.model.Cliente;
import com.casualidad.casualidad_backend.clientes.repository.ClienteRepository;
import com.casualidad.casualidad_backend.common.domain.enums.EstadoPedido;
import com.casualidad.casualidad_backend.auth.entity.Usuario;
import com.casualidad.casualidad_backend.auth.repository.UsuarioRepository;
import com.casualidad.casualidad_backend.clientes.domain.exception.RecursoNoEncontradoException;
import com.casualidad.casualidad_backend.pedidos.domain.model.Pedido;
import com.casualidad.casualidad_backend.pedidos.domain.model.ProductoPedido;
import com.casualidad.casualidad_backend.pedidos.dto.request.CrearPedidoDto;
import com.casualidad.casualidad_backend.pedidos.dto.request.DetallePedidoRequestDto;
import com.casualidad.casualidad_backend.pedidos.dto.response.PedidoResponseDto;
import com.casualidad.casualidad_backend.pedidos.repository.PedidoRepository;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;

import org.checkerframework.checker.units.qual.t;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrearPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    @Transactional
    public PedidoResponseDto ejecutar(CrearPedidoDto request) {

        // 1. Validar Cliente (HU6)
        Cliente cliente = clienteRepository.findById(request.idCliente())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));

        // 2. Validar Usuario Creador (Admin/Empleado)
        Usuario usuarioCreador = usuarioRepository.findById(request.idUsuario())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario administrador no encontrado"));

        // 3. Optimización: Traer productos de golpe
        List<Long> idsProductos = request.detalles().stream()
                .map(DetallePedidoRequestDto::idProducto).toList();
        Map<Long, Producto> mapaProductos = productoRepository.findAllById(idsProductos).stream()
                .collect(Collectors.toMap(Producto::getIdProducto, p -> p));

        // 4. Instanciar Pedido Inicial (Estado: PENDIENTE_ABONO)
        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .usuario(usuarioCreador) // Agregamos el admin responsable
                .creadoEn(LocalDateTime.now())
                .fechaEntrega(request.fechaEntrega())
                .estadoPedido(EstadoPedido.PENDIENTE)
                .codigoUnico(null) // Se genera después al confirmar abono
                .detalles(new ArrayList<>())
                .build();

        BigDecimal precioTotal = BigDecimal.ZERO;

        // 5. Mapear Detalles con Observaciones
        for (DetallePedidoRequestDto dto : request.detalles()) {
            Producto producto = mapaProductos.get(dto.idProducto());
            if (producto == null) throw new RecursoNoEncontradoException("Producto ID " + dto.idProducto() + " no existe");

            BigDecimal precioVenta = BigDecimal.valueOf(producto.getPrecioVenta());
            BigDecimal subtotal = precioVenta.multiply(BigDecimal.valueOf(dto.cantidad()));

            ProductoPedido detalle = ProductoPedido.builder()
                    .pedido(pedido)
                    .producto(producto)
                    .cantidad(dto.cantidad()) // Guardamos como BigDecimal internamente por precisión
                    .precioUnitario(precioVenta)
                    .subtotal(subtotal)
                    .observaciones(dto.observaciones()) // <-- HU18: Detalle de personalización
                    .build();

            pedido.getDetalles().add(detalle);
            precioTotal = precioTotal.add(subtotal);
        }

        pedido.setTotal(precioTotal);
        pedido.setSaldoPendiente(precioTotal);

        // 6. Guardar (No afecta inventario todavía)
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        return mapearAResponse(pedidoGuardado);
    }

    private PedidoResponseDto mapearAResponse(Pedido pedido) {
        // ... (Mapeo a DTO de respuesta similar al anterior pero incluyendo usuario creador si deseas) ...
        return PedidoResponseDto.builder()
                .idPedido(pedido.getIdPedido())
                .estado(pedido.getEstadoPedido().name())
                .precioTotal(pedido.getTotal())
                .fechaRegistro(pedido.getCreadoEn())
                .saldoPendiente(pedido.getSaldoPendiente())
                .build();
    }
}