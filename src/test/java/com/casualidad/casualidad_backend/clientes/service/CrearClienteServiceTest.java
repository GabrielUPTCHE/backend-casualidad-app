package com.casualidad.casualidad_backend.clientes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.casualidad.casualidad_backend.clientes.domain.exception.TelefonoDuplicadoException;
import com.casualidad.casualidad_backend.clientes.domain.model.Cliente;
import com.casualidad.casualidad_backend.clientes.domain.model.TelefonoCliente;
import com.casualidad.casualidad_backend.clientes.dto.request.ClienteRequestDto;
import com.casualidad.casualidad_backend.clientes.dto.response.ClienteResponseDto;
import com.casualidad.casualidad_backend.clientes.repository.ClienteRepository;
import com.casualidad.casualidad_backend.clientes.repository.TelefonoClienteRepository;

@ExtendWith(MockitoExtension.class)
class CrearClienteServiceTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private TelefonoClienteRepository telefonoRepository;

    @InjectMocks private CrearClienteService crearClienteService;

    @Test
    void registrarClienteDebeEliminarTelefonosDuplicadosEnLaPeticion() {
        ClienteRequestDto dto = new ClienteRequestDto("Ana Perez", List.of("111", "111", "222"), "Calle 1");
        when(telefonoRepository.findByNumeroTelefonoIn(dto.telefonos())).thenReturn(List.of());
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClienteResponseDto response = crearClienteService.registrarCliente(dto);

        assertEquals("Ana Perez", response.nombre());
        assertEquals(List.of("111", "222"), response.telefonos());

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertEquals(2, captor.getValue().getTelefonos().size());
    }

    @Test
    void registrarClienteDebeRechazarTelefonosYaExistentes() {
        ClienteRequestDto dto = new ClienteRequestDto("Ana Perez", List.of("111"), "Calle 1");
        when(telefonoRepository.findByNumeroTelefonoIn(dto.telefonos())).thenReturn(List.of(TelefonoCliente.builder().numeroTelefono("111").build()));

        assertThrows(TelefonoDuplicadoException.class, () -> crearClienteService.registrarCliente(dto));
    }
}