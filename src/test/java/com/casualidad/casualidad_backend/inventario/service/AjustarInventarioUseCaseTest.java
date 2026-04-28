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
import com.casualidad.casualidad_backend.inventario.dto.request.AjusteInventarioDto;
import com.casualidad.casualidad_backend.inventario.repository.MovimientoInventarioRepository;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.domain.model.UnidadMedida;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class AjustarInventarioUseCaseTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private MovimientoInventarioRepository movimientoRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private AjustarInventarioUseCase ajustarInventarioUseCase;

    @Test
    void ejecutarDebeProcesarUnAjusteNormal() {
        Producto producto = producto(1L, "Harina", new BigDecimal("10.000"));
        AjusteInventarioDto dto = new AjusteInventarioDto(1L, new BigDecimal("12.000"), "Conteo físico");

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String mensaje = ajustarInventarioUseCase.ejecutar(dto);

        assertEquals("Ajuste de inventario procesado correctamente.", mensaje);
        assertEquals(new BigDecimal("12.000"), producto.getCantidad());

        ArgumentCaptor<MovimientoInventario> movimientoCaptor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoRepository).save(movimientoCaptor.capture());
        assertEquals(new BigDecimal("2.000"), movimientoCaptor.getValue().getCantidad());
        assertEquals(TipoMovimiento.AJUSTE, movimientoCaptor.getValue().getTipoMovimiento());
        assertEquals(MotivoMovimiento.AJUSTE_INVENTARIO, movimientoCaptor.getValue().getMotivo());

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        InventarioActualizadoEvent event = (InventarioActualizadoEvent) eventCaptor.getValue();
        assertEquals(new BigDecimal("12.000"), event.nuevoStockTotal());
    }

    @Test
    void ejecutarDebeAvisarCuandoElStockQuedaEnCero() {
        Producto producto = producto(1L, "Harina", new BigDecimal("4.000"));
        AjusteInventarioDto dto = new AjusteInventarioDto(1L, BigDecimal.ZERO, "Merma total");

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String mensaje = ajustarInventarioUseCase.ejecutar(dto);

        assertEquals("Ajuste realizado. ¡ALERTA! El stock de 'Harina' se ha agotado.", mensaje);
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