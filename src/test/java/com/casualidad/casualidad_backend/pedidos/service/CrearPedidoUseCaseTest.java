package com.casualidad.casualidad_backend.pedidos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.casualidad.casualidad_backend.auth.repository.UsuarioRepository;
import com.casualidad.casualidad_backend.clientes.domain.exception.RecursoNoEncontradoException;
import com.casualidad.casualidad_backend.clientes.domain.model.Cliente;
import com.casualidad.casualidad_backend.clientes.repository.ClienteRepository;
import com.casualidad.casualidad_backend.pedidos.domain.model.Pedido;
import com.casualidad.casualidad_backend.pedidos.dto.request.CrearPedidoDto;
import com.casualidad.casualidad_backend.pedidos.dto.request.DetallePedidoRequestDto;
import com.casualidad.casualidad_backend.pedidos.dto.response.PedidoResponseDto;
import com.casualidad.casualidad_backend.pedidos.repository.PedidoRepository;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.domain.model.UnidadMedida;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class CrearPedidoUseCaseTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ProductoRepository productoRepository;

    @InjectMocks private CrearPedidoUseCase crearPedidoUseCase;

    @Test
    void ejecutarDebeCrearElPedidoConTotalesCorrectos() {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(1L);
        Usuario usuario = usuario(2L);
        Producto producto = producto(10L, "Brownie", 1500);
        CrearPedidoDto request = new CrearPedidoDto(1L, 2L, LocalDate.now(), List.of(new DetallePedidoRequestDto(10L, 2, "Sin nueces")));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findAllById(List.of(10L))).thenReturn(List.of(producto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setIdPedido(99L);
            return pedido;
        });

        PedidoResponseDto response = crearPedidoUseCase.ejecutar(request);

        assertEquals(99L, response.idPedido());
        assertEquals("PENDIENTE", response.estado());
        assertEquals(new BigDecimal("3000"), response.precioTotal());

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getDetalles().size());
        assertEquals(new BigDecimal("3000"), captor.getValue().getTotal());
    }

    @Test
    void ejecutarDebeLanzarSiElClienteNoExiste() {
        CrearPedidoDto request = new CrearPedidoDto(1L, 2L, LocalDate.now(), List.of(new DetallePedidoRequestDto(10L, 1, null)));
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> crearPedidoUseCase.ejecutar(request));
    }

    private Usuario usuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId_usuario(id);
        usuario.setNombre("Admin");
        usuario.setCorreo("admin@casualidad.com");
        usuario.setActivo(true);
        usuario.setRol(new Rol(1L, "ADMIN", "ALTO", true));
        return usuario;
    }

    private Producto producto(Long id, String nombre, Integer precioVenta) {
        return Producto.builder()
                .idProducto(id)
                .nombre(nombre)
                .precioVenta(precioVenta)
                .cantidad(BigDecimal.ZERO)
                .unidadMedida(new UnidadMedida(1L, "und"))
                .build();
    }
}