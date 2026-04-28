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

import com.casualidad.casualidad_backend.common.domain.enums.TipoProducto;
import com.casualidad.casualidad_backend.productos.domain.exception.ComposicionVaciaException;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.domain.model.UnidadMedida;
import com.casualidad.casualidad_backend.productos.dto.InsumoComposicionDto;
import com.casualidad.casualidad_backend.productos.dto.response.ComposicionResponseDto;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class GestionarComposicionUseCaseTest {

    @Mock private ProductoRepository productoRepository;

    @InjectMocks private GestionarComposicionUseCase gestionarComposicionUseCase;

    @Test
    void ejecutarDebeReemplazarLaComposicionYCalcularCosto() {
        Producto padre = producto(1L, "Brownie", TipoProducto.ELABORADO, 0);
        Producto insumo = producto(2L, "Harina", TipoProducto.INSUMO, 10);
        InsumoComposicionDto dto = new InsumoComposicionDto(2L, new BigDecimal("3.000"));

        when(productoRepository.findById(1L)).thenReturn(Optional.of(padre));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(insumo));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ComposicionResponseDto response = gestionarComposicionUseCase.ejecutar(1L, List.of(dto));

        assertEquals(1L, response.idProductoPadre());
        assertEquals(new BigDecimal("30.000"), response.costoTotalProduccion());
        assertEquals(1, response.insumos().size());
        verify(productoRepository).save(padre);
        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getComposicion().size());
    }

    @Test
    void ejecutarDebeFallarConComposicionVacia() {
        assertThrows(ComposicionVaciaException.class, () -> gestionarComposicionUseCase.ejecutar(1L, List.of()));
    }

    private Producto producto(Long id, String nombre, TipoProducto tipo, Integer precioCompra) {
        Producto producto = Producto.builder()
                .idProducto(id)
                .nombre(nombre)
                .tipo(tipo)
                .precioCompra(precioCompra)
                .unidadMedida(new UnidadMedida(1L, "und"))
                .build();
        return producto;
    }
}