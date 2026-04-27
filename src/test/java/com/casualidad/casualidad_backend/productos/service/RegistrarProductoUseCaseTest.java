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
import com.casualidad.casualidad_backend.productos.domain.exception.ProductoDeReventaException;
import com.casualidad.casualidad_backend.productos.domain.exception.YaExisteProductoException;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.domain.model.UnidadMedida;
import com.casualidad.casualidad_backend.productos.dto.request.ProductoRequestDto;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;
import com.casualidad.casualidad_backend.productos.repository.UnidadMedidaRepository;

@ExtendWith(MockitoExtension.class)
class RegistrarProductoUseCaseTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private UnidadMedidaRepository unidadMedidaRepository;

    @InjectMocks private RegistrarProductoUseCase registrarProductoUseCase;

    @Test
    void ejecutarDebeCrearProductoConNuevaUnidad() {
        ProductoRequestDto dto = new ProductoRequestDto("Brownie", TipoProducto.INSUMO, null, "kg", new BigDecimal("0.500"), new BigDecimal("0.100"), 2000, null, new BigDecimal("5"));
        UnidadMedida unidad = new UnidadMedida(1L, "kg");

        when(productoRepository.existsByNombreIgnoreCase("Brownie")).thenReturn(false);
        //when(productoRepository.existsByTipo(TipoProducto.INSUMO)).thenReturn(true);
        when(unidadMedidaRepository.findByNombreIgnoreCase("kg")).thenReturn(Optional.of(unidad));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto producto = invocation.getArgument(0);
            producto.setIdProducto(11L);
            return producto;
        });

        Long id = registrarProductoUseCase.ejecutar(dto);

        assertEquals(11L, id);
        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        assertEquals("Brownie", captor.getValue().getNombre());
        assertEquals(new BigDecimal("0.500"), captor.getValue().getCantidad());
    }

    @Test
    void ejecutarDebeRechazarProductosDuplicados() {
        ProductoRequestDto dto = new ProductoRequestDto("Brownie", TipoProducto.INSUMO, null, "kg", BigDecimal.ONE, BigDecimal.ZERO, 2000, null, BigDecimal.ONE);

        when(productoRepository.existsByNombreIgnoreCase("Brownie")).thenReturn(true);

        assertThrows(YaExisteProductoException.class, () -> registrarProductoUseCase.ejecutar(dto));
    }

    @Test
    void ejecutarDebeRechazarReventaSinPrecioVenta() {
        ProductoRequestDto dto = new ProductoRequestDto("Camiseta", TipoProducto.REVENTA, null, "und", BigDecimal.ONE, BigDecimal.ZERO, 1000, null, BigDecimal.ZERO);

        when(productoRepository.existsByNombreIgnoreCase("Camiseta")).thenReturn(false);

        assertThrows(ProductoDeReventaException.class, () -> registrarProductoUseCase.ejecutar(dto));
    }
}