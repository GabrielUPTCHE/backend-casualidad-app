package com.casualidad.casualidad_backend.inventario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.casualidad.casualidad_backend.common.domain.enums.MotivoMovimiento;
import com.casualidad.casualidad_backend.common.domain.enums.TipoMovimiento;
import com.casualidad.casualidad_backend.common.domain.event.InventarioActualizadoEvent;
import com.casualidad.casualidad_backend.inventario.domain.model.MovimientoInventario;
import com.casualidad.casualidad_backend.inventario.dto.request.EntradaInventarioDto;
import com.casualidad.casualidad_backend.inventario.repository.MovimientoInventarioRepository;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.domain.model.UnidadMedida;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class RegistrarEntradaInventarioUseCaseTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private MovimientoInventarioRepository movimientoRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RegistrarEntradaInventarioUseCase registrarEntradaInventarioUseCase;

    @Test
    void ejecutarDebeAumentarStockRegistrarMovimientoYPublicarEvento() {
        Producto producto = producto(1L, "Harina", new BigDecimal("10.000"));
        EntradaInventarioDto dto = new EntradaInventarioDto(
                1L,
                new BigDecimal("5.000"),
                MotivoMovimiento.COMPRA_INSUMOS);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        registrarEntradaInventarioUseCase.ejecutar(dto);

        assertEquals(new BigDecimal("15.000"), producto.getCantidad());

        ArgumentCaptor<MovimientoInventario> movimientoCaptor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoRepository).save(movimientoCaptor.capture());
        assertEquals(TipoMovimiento.ENTRADA, movimientoCaptor.getValue().getTipoMovimiento());

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        InventarioActualizadoEvent event = (InventarioActualizadoEvent) eventCaptor.getValue();
        assertEquals(1L, event.idProducto());
        assertEquals(new BigDecimal("5.000"), event.cantidadModificada());
    }

    private Producto producto(Long id, String nombre, BigDecimal cantidad) {
        return Producto.builder()
                .idProducto(id)
                .nombre(nombre)
                .cantidad(cantidad)
                .stockMinimo(BigDecimal.ZERO)
                .unidadMedida(new UnidadMedida(1L, "kg"))
                .build();
    }
}