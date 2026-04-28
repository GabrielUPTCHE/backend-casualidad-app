package com.casualidad.casualidad_backend.productos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.casualidad.casualidad_backend.common.domain.enums.TipoProducto;
import com.casualidad.casualidad_backend.inventario.domain.exception.ProductoNoExisteException;
import com.casualidad.casualidad_backend.productos.domain.exception.ProductoDeReventaException;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.domain.model.UnidadMedida;
import com.casualidad.casualidad_backend.productos.dto.request.EditarProductoDto;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;
import com.casualidad.casualidad_backend.productos.repository.UnidadMedidaRepository;

@ExtendWith(MockitoExtension.class)
class EditarProductoUseCaseTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private UnidadMedidaRepository unidadMedidaRepository;

    @InjectMocks private EditarProductoUseCase editarProductoUseCase;

    @Test
    void ejecutarDebeActualizarCamposEditables() {
        Producto producto = Producto.builder()
                .idProducto(1L)
                .nombre("Brownie")
                .tipo(TipoProducto.INSUMO)
                .precioCompra(2000)
                .precioVenta(3000)
                .stockMinimo(new BigDecimal("1.000"))
                .unidadMedida(new UnidadMedida(1L, "und"))
                .build();
        EditarProductoDto dto = new EditarProductoDto("Brownie Premium", null, null, new BigDecimal("2.000"), 2500, 3500, new BigDecimal("7"));

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.existsByNombreIgnoreCaseAndIdProductoNot("Brownie Premium", 1L)).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        editarProductoUseCase.ejecutar(1L, dto);

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        assertEquals("Brownie Premium", captor.getValue().getNombre());
        assertEquals(Integer.valueOf(2500), captor.getValue().getPrecioCompra());
    }

    @Test
    void ejecutarDebeRechazarPrecioVentaNuloEnReventa() {
        Producto producto = Producto.builder()
                .idProducto(1L)
                .nombre("Camiseta")
                .tipo(TipoProducto.REVENTA)
                .unidadMedida(new UnidadMedida(1L, "und"))
                .build();
        EditarProductoDto dto = new EditarProductoDto("Camiseta Nueva", null, null, BigDecimal.ONE, 1000, null, BigDecimal.ZERO);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.existsByNombreIgnoreCaseAndIdProductoNot("Camiseta Nueva", 1L)).thenReturn(false);

        assertThrows(ProductoDeReventaException.class, () -> editarProductoUseCase.ejecutar(1L, dto));
    }

    @Test
    void ejecutarDebeLanzarSiElProductoNoExiste() {
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductoNoExisteException.class, () -> editarProductoUseCase.ejecutar(1L, new EditarProductoDto("X", null, null, null, null, null, null)));
    }
}