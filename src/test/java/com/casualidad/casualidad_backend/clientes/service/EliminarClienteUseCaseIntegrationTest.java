package com.casualidad.casualidad_backend.clientes.service;

import com.casualidad.casualidad_backend.auth.entity.Usuario;
import com.casualidad.casualidad_backend.auth.repository.UsuarioRepository;
import com.casualidad.casualidad_backend.clientes.domain.model.Cliente;
import com.casualidad.casualidad_backend.clientes.domain.model.TelefonoCliente;
import com.casualidad.casualidad_backend.clientes.repository.ClienteRepository;
import com.casualidad.casualidad_backend.pedidos.domain.model.Pedido;
import com.casualidad.casualidad_backend.pedidos.repository.PedidoRepository;
import com.casualidad.casualidad_backend.common.domain.enums.EstadoPedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"})
@Transactional
public class EliminarClienteUseCaseIntegrationTest {

    @Autowired
    private EliminarClienteUseCase eliminarClienteUseCase;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Cliente clienteConPedidos;

    @BeforeEach
    void setUp() {
        pedidoRepository.deleteAll();
        clienteRepository.deleteAll();
        usuarioRepository.deleteAll();

        clienteConPedidos = Cliente.builder()
                .nombre("Carlos Ruiz")
                .direccion("Barrio Central 10")
                .activo(true)
                .build();

        clienteConPedidos = clienteRepository.save(clienteConPedidos);

        // Usuario que crea los pedidos (dummy)
        Usuario usuario = new Usuario();
        usuario.setNombre("Sistema");
        usuario.setCorreo("sistema@local");
        usuario.setContraseña("x");
        usuario.setActivo(true);
        usuario = usuarioRepository.save(usuario);

        List<Pedido> pedidos = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            Pedido p = Pedido.builder()
                .codigoUnico("PED" + i + "-" + System.currentTimeMillis())
                .creadoEn(LocalDateTime.now())
                .estadoPedido(EstadoPedido.PENDIENTE)
                .fechaEntrega(LocalDate.now().plusDays(3))
                .total(BigDecimal.valueOf(100 + i))
                .saldoPendiente(BigDecimal.valueOf(100 + i))
                .cliente(clienteConPedidos)
                .usuario(usuario)
                .build();
            pedidos.add(p);
        }

        pedidoRepository.saveAll(pedidos);
    }

    @Test
    void eliminarCliente_conPedidos_debeMarcarInactivoYNoEliminarFisicamente() {
        Long id = clienteConPedidos.getIdCliente();

        String resultado = eliminarClienteUseCase.ejecutar(id);

        assertThat(resultado).contains("marcado como INACTIVO");

        // Cliente sigue en BD pero inactivo
        Cliente cliente = clienteRepository.findById(id).orElse(null);
        assertThat(cliente).isNotNull();
        assertThat(cliente.getActivo()).isFalse();

        // Los pedidos siguen asociados
        long contadorPedidos = pedidoRepository.countByClienteIdCliente(id);
        assertThat(contadorPedidos).isEqualTo(5);
    }
}
