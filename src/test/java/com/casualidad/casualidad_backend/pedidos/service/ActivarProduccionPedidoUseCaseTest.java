package com.casualidad.casualidad_backend.pedidos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.casualidad.casualidad_backend.auth.entity.Rol;
import com.casualidad.casualidad_backend.auth.entity.Usuario;
import com.casualidad.casualidad_backend.clientes.domain.model.Cliente;
import com.casualidad.casualidad_backend.common.domain.enums.EstadoPedido;
import com.casualidad.casualidad_backend.inventario.domain.model.MovimientoInventario;
import com.casualidad.casualidad_backend.inventario.repository.MovimientoInventarioRepository;
import com.casualidad.casualidad_backend.pedidos.domain.model.Pedido;
import com.casualidad.casualidad_backend.pedidos.domain.model.ProductoPedido;
import com.casualidad.casualidad_backend.pedidos.repository.PedidoRepository;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.domain.model.UnidadMedida;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ActivarProduccionPedidoUseCaseTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private MovimientoInventarioRepository movimientoRepository;

    @InjectMocks
    private ActivarProduccionPedidoUseCase activarProduccionPedidoUseCase;

    @Test
    void ejecutarDebeDescontarInventarioYGenerarCodigo() {
        Producto producto = producto(10L, "Brownie", new BigDecimal("20.000"));
        Pedido pedido = pedidoConDetalle(1L, producto, 3);

        when(pedidoRepository.findByIdWithDetallesCompletos(1L)).thenReturn(Optional.of(pedido));
        when(productoRepository.findAllById(any())).thenReturn(List.of(producto));
        when(productoRepository.saveAll(any()))
                .thenAnswer(invocation -> {
                    Iterable<Producto> iterable = invocation.getArgument(0);
                    List<Producto> lista = new ArrayList<>();
                    iterable.forEach(lista::add);
                    return lista;
                });
        when(movimientoRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pedidoRepository.countByCreadoEnBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);

        String codigo = activarProduccionPedidoUseCase.ejecutar(1L);

        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        assertEquals(fecha + "-JP-01", codigo);
        assertEquals(EstadoPedido.EN_PRODUCCION, pedido.getEstadoPedido());
        assertEquals(codigo, pedido.getCodigoUnico());
        assertEquals(new BigDecimal("17.000"), producto.getCantidad());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MovimientoInventario>> movimientosCaptor = ArgumentCaptor.forClass(List.class);
        verify(movimientoRepository).saveAll(movimientosCaptor.capture());
        assertEquals(1, movimientosCaptor.getValue().size());
        assertEquals(new BigDecimal("-3"), movimientosCaptor.getValue().get(0).getCantidad());
    }

    @Test
    void ejecutarDebeFallarSiElPedidoNoExiste() {
        when(pedidoRepository.findByIdWithDetallesCompletos(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> activarProduccionPedidoUseCase.ejecutar(1L));
    }

    private Pedido pedidoConDetalle(Long idPedido, Producto producto, Integer cantidad) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNombre("Juan Perez");

        Usuario usuario = new Usuario();
        usuario.setId_usuario(2L);
        usuario.setNombre("Admin");
        usuario.setCorreo("admin@casualidad.com");
        usuario.setActivo(true);
        usuario.setRol(new Rol(1L, "ADMIN", "ALTO", true));

        Pedido pedido = Pedido.builder()
                .idPedido(idPedido)
                .cliente(cliente)
                .usuario(usuario)
                .creadoEn(LocalDateTime.now())
                .fechaEntrega(LocalDate.now())
                .estadoPedido(EstadoPedido.PENDIENTE)
                .total(new BigDecimal("60.000"))
                .saldoPendiente(new BigDecimal("60.000"))
                .build();

        pedido.getDetalles().add(ProductoPedido.builder()
                .pedido(pedido)
                .producto(producto)
                .cantidad(cantidad)
                .precioUnitario(new BigDecimal("20.000"))
                .subtotal(new BigDecimal("60.000"))
                .build());

        return pedido;
    }

    private Producto producto(Long id, String nombre, BigDecimal cantidad) {
        return Producto.builder()
                .idProducto(id)
                .nombre(nombre)
                .cantidad(cantidad)
                .unidadMedida(new UnidadMedida(1L, "und"))
                .build();
    }
}