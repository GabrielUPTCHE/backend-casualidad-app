package com.casualidad.casualidad_backend.clientes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.casualidad.casualidad_backend.clientes.domain.exception.RecursoNoEncontradoException;
import com.casualidad.casualidad_backend.clientes.domain.exception.TelefonoDuplicadoException;
import com.casualidad.casualidad_backend.clientes.domain.model.Cliente;
import com.casualidad.casualidad_backend.clientes.domain.model.TelefonoCliente;
import com.casualidad.casualidad_backend.clientes.dto.request.ClienteRequestDto;
import com.casualidad.casualidad_backend.clientes.dto.response.ClienteResponseDto;
import com.casualidad.casualidad_backend.clientes.repository.ClienteRepository;
import com.casualidad.casualidad_backend.clientes.repository.TelefonoClienteRepository;

@ExtendWith(MockitoExtension.class)
class ActualizarClienteUseCaseTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private TelefonoClienteRepository telefonoRepository;

    @InjectMocks private ActualizarClienteUseCase actualizarClienteUseCase;

    @Test
    void ejecutarDebeActualizarDatosYReemplazarTelefonos() {
        Cliente cliente = clienteConTelefonos(1L, "Ana", "Calle 1", List.of("111"));
        ClienteRequestDto dto = new ClienteRequestDto("Ana Maria", List.of("222", "222", "333"), "Calle 2");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(telefonoRepository.findTelefonosDeOtrosClientes(dto.telefonos(), 1L)).thenReturn(List.of());
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClienteResponseDto response = actualizarClienteUseCase.ejecutar(1L, dto);

        assertEquals("Ana Maria", response.nombre());
        assertEquals(List.of("222", "333"), response.telefonos());

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertEquals(2, captor.getValue().getTelefonos().size());
    }

    @Test
    void ejecutarDebeLanzarSiElClienteNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> actualizarClienteUseCase.ejecutar(99L, new ClienteRequestDto("A", List.of("1"), "C")));
    }

    @Test
    void ejecutarDebeRechazarTelefonosDeOtrosClientes() {
        Cliente cliente = clienteConTelefonos(1L, "Ana", "Calle 1", List.of("111"));
        ClienteRequestDto dto = new ClienteRequestDto("Ana Maria", List.of("222"), "Calle 2");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(telefonoRepository.findTelefonosDeOtrosClientes(dto.telefonos(), 1L)).thenReturn(List.of(TelefonoCliente.builder().numeroTelefono("222").build()));

        assertThrows(TelefonoDuplicadoException.class, () -> actualizarClienteUseCase.ejecutar(1L, dto));
    }

    private Cliente clienteConTelefonos(Long id, String nombre, String direccion, List<String> telefonos) {
        Cliente cliente = Cliente.builder().idCliente(id).nombre(nombre).direccion(direccion).activo(true).build();
        telefonos.forEach(numero -> cliente.getTelefonos().add(TelefonoCliente.builder().numeroTelefono(numero).cliente(cliente).build()));
        return cliente;
    }
}