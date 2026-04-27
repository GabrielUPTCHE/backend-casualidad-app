package com.casualidad.casualidad_backend.productos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.casualidad.casualidad_backend.clientes.domain.exception.RecursoNoEncontradoException;
import com.casualidad.casualidad_backend.common.domain.enums.TipoProducto;
import com.casualidad.casualidad_backend.productos.domain.exception.ComposicionVaciaException;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.domain.model.UnidadMedida;
import com.casualidad.casualidad_backend.productos.dto.InsumoComposicionDto;
import com.casualidad.casualidad_backend.productos.dto.response.ComposicionResponseDto;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class AgregarInsumosProductoUseCaseTest {

    @Mock private ProductoRepository productoRepository;

    @InjectMocks private AgregarInsumosProductoUseCase agregarInsumosProductoUseCase;

    @Test
    void ejecutarDebeAgregarUnInsumoNuevoYRecalcularCosto() {
        Producto padre = producto(1L, "Brownie", TipoProducto.ELABORADO, null);
        Producto insumo = producto(2L, "Harina", TipoProducto.INSUMO, 10);
        InsumoComposicionDto dto = new InsumoComposicionDto(2L, new BigDecimal("2.000"));

        when(productoRepository.findById(1L)).thenReturn(Optional.of(padre));
        when(productoRepository.findAllById(List.of(2L))).thenReturn(List.of(insumo));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ComposicionResponseDto response = agregarInsumosProductoUseCase.ejecutar(1L, List.of(dto));

        assertEquals(new BigDecimal("20.000"), response.costoTotalProduccion());
        assertEquals(1, response.insumos().size());
        assertEquals(1, padre.getComposicion().size());

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        assertEquals(Integer.valueOf(20), captor.getValue().getPrecioCompra());
    }

    @Test
    void ejecutarDebeFallarSiNoVieneComposicion() {
        assertThrows(ComposicionVaciaException.class, () -> agregarInsumosProductoUseCase.ejecutar(1L, List.of()));
    }

    @Test
    void ejecutarDebeFallarSiElProductoPadreNoExiste() {
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> agregarInsumosProductoUseCase.ejecutar(1L, List.of(new InsumoComposicionDto(2L, BigDecimal.ONE))));
    }

    private Producto producto(Long id, String nombre, TipoProducto tipo, Integer precioCompra) {
        return Producto.builder()
                .idProducto(id)
                .nombre(nombre)
                .tipo(tipo)
                .precioCompra(precioCompra)
                .unidadMedida(new UnidadMedida(1L, "und"))
                .build();
    }
}