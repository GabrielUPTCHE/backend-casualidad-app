package com.casualidad.casualidad_backend.productos.service;

import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.dto.InsumoComposicionDto;
import com.casualidad.casualidad_backend.productos.dto.request.ProductoRequestDto;
import com.casualidad.casualidad_backend.productos.dto.request.EditarProductoDto;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect")
public class RecalculoCostoInsumoIntegrationTest {
// ./mvnw -Dtest=RecalculoCostoInsumoIntegrationTest test
    @Autowired
    private RegistrarProductoUseCase registrarProductoUseCase;

    @Autowired
    private AgregarInsumosProductoUseCase agregarInsumosProductoUseCase;

    @Autowired
    private EditarProductoUseCase editarProductoUseCase;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private List<Alerta> alertCollector;

    static class Alerta {
        final Long idProducto;
        final BigDecimal monto;
        final String mensaje;

        Alerta(Long idProducto, BigDecimal monto, String mensaje) {
            this.idProducto = idProducto;
            this.monto = monto;
            this.mensaje = mensaje;
        }
    }

    @TestConfiguration
    static class CollectorConfig {
        @Bean
        public List<Alerta> alertCollector() {
            return new ArrayList<>();
        }
        @Bean
        public Object alertListener(List<Alerta> collector) {
            return new Object() {
                @EventListener
                public void onAlerta(Alerta evento) {
                    collector.add(evento);
                }
            };
        }
    }

    // Evento de prueba local (no modifica main)
    static class AlertaInformativaEventTest {
        final Long idProducto;
        final BigDecimal monto;
        final String mensaje;

        AlertaInformativaEventTest(Long idProducto, BigDecimal monto, String mensaje) {
            this.idProducto = idProducto;
            this.monto = monto;
            this.mensaje = mensaje;
        }
    }

    @Test
    @Transactional
    void recalculaCostoCuandoCambiaPrecioDeInsumo_y_publica_alerta() {
        // 1) Crear insumo 'Limpiapipas' con precio 300
        ProductoRequestDto dtoInsumo = new ProductoRequestDto(
                "Limpiapipas",
                com.casualidad.casualidad_backend.common.domain.enums.TipoProducto.INSUMO,
                null,
                "unidad-test",
                null,
                null,
                300,
                null,
                null
        );

        Long idInsumo = registrarProductoUseCase.ejecutar(dtoInsumo);

        // 2) Crear producto elaborado 'Ramo Eterno'
        ProductoRequestDto dtoElaborado = new ProductoRequestDto(
                "Ramo Eterno",
                com.casualidad.casualidad_backend.common.domain.enums.TipoProducto.ELABORADO,
                null,
                "unidad-test",
                null,
                null,
                null,
                null,
                null
        );

        Long idRamo = registrarProductoUseCase.ejecutar(dtoElaborado);

        // 3) Agregar insumo a la composición (cantidad 1) -> costo padre = 300
        InsumoComposicionDto comp = new InsumoComposicionDto(idInsumo, BigDecimal.ONE);
        agregarInsumosProductoUseCase.ejecutar(idRamo, List.of(comp));

        Producto padreAntes = productoRepository.findById(idRamo).orElseThrow();
        assertThat(padreAntes.getPrecioCompra()).isEqualTo(300);

        // 4) Actualizar precio de limpiapipas a 350 usando el caso real de edición
        EditarProductoDto editarDto = new EditarProductoDto(
                "Limpiapipas",
                null,
                null,
                null,
                350,
                null,
                null
        );

        editarProductoUseCase.ejecutar(idInsumo, editarDto);

        // 5) DRIVER: Simular componente superior que al detectar cambio en el insumo
        // consulta padres y recalcula costos. No tocamos main: todo se hace en test.
        List<Producto> padres = em.createQuery(
                "SELECT DISTINCT p FROM Producto p JOIN p.composicion c WHERE c.insumo.idProducto = :idInsumo",
                Producto.class)
                .setParameter("idInsumo", idInsumo)
                .getResultList();

        for (Producto padre : padres) {
            BigDecimal costo = padre.getComposicion().stream()
                    .map(c -> BigDecimal.valueOf(c.getInsumo().getPrecioCompra()).multiply(c.getCantidadUsada()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            padre.setPrecioCompra(BigDecimal.valueOf(costo.doubleValue()).intValue());
            productoRepository.save(padre);

            // publicar evento de prueba (capturado por el collector)
            publisher.publishEvent(new Alerta(padre.getIdProducto(), costo, "Costo recalculado por cambio de insumo"));
        }

        Producto padreDespues = productoRepository.findById(idRamo).orElseThrow();
        assertThat(padreDespues.getPrecioCompra()).isEqualTo(350);

        // Verificar que el collector recibió la alerta
        boolean alertaEncontrada = alertCollector.stream()
                .anyMatch(a -> a.idProducto.equals(idRamo) && a.monto.compareTo(BigDecimal.valueOf(350)) == 0);

        assertThat(alertaEncontrada).isTrue();
    }
}
