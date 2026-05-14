package com.casualidad.casualidad_backend.clientes.service;

import com.casualidad.casualidad_backend.clientes.domain.exception.TelefonoDuplicadoException;
import com.casualidad.casualidad_backend.clientes.dto.request.ClienteRequestDto;
import com.casualidad.casualidad_backend.clientes.dto.response.ClienteResponseDto;
import com.casualidad.casualidad_backend.clientes.domain.model.Cliente;
import com.casualidad.casualidad_backend.clientes.domain.model.TelefonoCliente;
import com.casualidad.casualidad_backend.clientes.repository.ClienteRepository;
import com.casualidad.casualidad_backend.clientes.repository.TelefonoClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"})
@Transactional
public class CrearClienteServiceIntegrationTest {

    @Autowired
    private CrearClienteService clienteService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private TelefonoClienteRepository telefonoRepository;

    @BeforeEach
    void setUp() {
        clienteRepository.deleteAll();
        telefonoRepository.deleteAll();

        Cliente maria = Cliente.builder()
                .nombre("María López")
                .direccion("Calle Falsa 123")
                .activo(true)
                .build();

        TelefonoCliente tel = TelefonoCliente.builder()
                .numeroTelefono("3001234567")
                .cliente(maria)
                .build();

        maria.getTelefonos().add(tel);

        clienteRepository.save(maria);
    }

    @Test
    void registrarCliente_conTelefonoDuplicado_debeLanzarTelefonoDuplicadoException() {
        ClienteRequestDto nuevo = new ClienteRequestDto(
                "Pedro García",
                List.of("3001234567"),
                "Avenida Siempre Viva 742"
        );

        assertThatThrownBy(() -> clienteService.registrarCliente(nuevo))
                .isInstanceOf(TelefonoDuplicadoException.class)
                .hasMessageContaining("Uno o más números de teléfono ya están registrados");

        // Aseguramos que no se creó un nuevo cliente con ese teléfono
        List<TelefonoCliente> telefonos = telefonoRepository.findByNumeroTelefonoIn(List.of("3001234567"));
        assertThat(telefonos).hasSize(1);
    }
}
