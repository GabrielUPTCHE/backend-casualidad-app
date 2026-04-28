package com.casualidad.casualidad_backend.clientes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.casualidad.casualidad_backend.clientes.domain.exception.RecursoNoEncontradoException;
import com.casualidad.casualidad_backend.clientes.domain.model.Cliente;
import com.casualidad.casualidad_backend.clientes.repository.ClienteRepository;
import com.casualidad.casualidad_backend.pedidos.repository.PedidoRepository;

@ExtendWith(MockitoExtension.class)
class EliminarClienteUseCaseTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private PedidoRepository pedidoRepository;

    @InjectMocks private EliminarClienteUseCase eliminarClienteUseCase;

    @Test
    void ejecutarDebeAplicarBorradoLogicoSiTienePedidos() {
        Cliente cliente = Cliente.builder().idCliente(1L).nombre("Ana").activo(true).build();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(pedidoRepository.countByClienteIdCliente(1L)).thenReturn(1L);

        String mensaje = eliminarClienteUseCase.ejecutar(1L);

        assertEquals("No se puede eliminar físicamente por historial de pedidos. Se ha marcado como INACTIVO.", mensaje);
        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertEquals(false, captor.getValue().getActivo());
        verify(clienteRepository, never()).delete(cliente);
    }

    @Test
    void ejecutarDebeEliminarFisicamenteSiNoTienePedidos() {
        Cliente cliente = Cliente.builder().idCliente(1L).nombre("Ana").activo(true).build();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(pedidoRepository.countByClienteIdCliente(1L)).thenReturn(0L);

        String mensaje = eliminarClienteUseCase.ejecutar(1L);

        assertEquals("Cliente eliminado físicamente del sistema con éxito.", mensaje);
        verify(clienteRepository).delete(cliente);
    }

    @Test
    void ejecutarDebeLanzarSiNoExisteElCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> eliminarClienteUseCase.ejecutar(1L));
    }
}