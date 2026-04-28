package com.casualidad.casualidad_backend.clientes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.casualidad.casualidad_backend.clientes.domain.model.Cliente;
import com.casualidad.casualidad_backend.clientes.domain.model.TelefonoCliente;
import com.casualidad.casualidad_backend.clientes.repository.ClienteRepository;
import com.casualidad.casualidad_backend.common.domain.dtos.response.PageResponse;

@ExtendWith(MockitoExtension.class)
class ListarClientesUseCaseTest {

    @Mock private ClienteRepository clienteRepository;

    @InjectMocks private ListarClientesUseCase listarClientesUseCase;

    @Test
    void ejecutarDebeMapearLaPaginaDeClientes() {
        Cliente cliente = Cliente.builder().idCliente(1L).nombre("Ana").direccion("Calle 1").activo(true).build();
        cliente.getTelefonos().add(TelefonoCliente.builder().numeroTelefono("111").cliente(cliente).build());
        when(clienteRepository.buscarActivosPorFiltro(any(), any())).thenReturn(new PageImpl<>(List.of(cliente), PageRequest.of(0, 10), 1));

        PageResponse<?> response = listarClientesUseCase.ejecutar(0, 10, "Ana");

        assertEquals(1L, response.getTotalElements());
        assertEquals(1, response.getData().size());
        assertEquals("Ana", ((com.casualidad.casualidad_backend.clientes.dto.response.ClienteListadoDto) response.getData().get(0)).nombre());
    }
}