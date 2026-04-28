package com.casualidad.casualidad_backend.productos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.casualidad.casualidad_backend.productos.domain.model.Item;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.dto.request.DefinirComposicionDto;
import com.casualidad.casualidad_backend.productos.dto.request.InsumoRequeridoDto;
import com.casualidad.casualidad_backend.productos.repository.ItemRepository;
import com.casualidad.casualidad_backend.productos.repository.ProductoItemRepository;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class DefinirComposicionUseCaseTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private ProductoItemRepository productoItemRepository;

    @InjectMocks private DefinirComposicionUseCase definirComposicionUseCase;

    @Test
    void ejecutarDebeCalcularCostoYGuardarComposicionYActualizarPrecioVenta() {
        Producto producto = producto(1L, "Brownie", TipoProducto.ELABORADO);
        Item item = new Item();
        item.setIdItem(2L);
        item.setNombre("Harina");
        item.setPrecioUnitario(new BigDecimal("10.00"));

        InsumoRequeridoDto insumoDto = new InsumoRequeridoDto(2L, new BigDecimal("3.000"));
        DefinirComposicionDto dto = new DefinirComposicionDto(1L, List.of(insumoDto));

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(itemRepository.findAllById(List.of(2L))).thenReturn(List.of(item));

        BigDecimal costo = definirComposicionUseCase.ejecutar(dto);

        assertEquals(0, costo.compareTo(new BigDecimal("30.000")));
        // Precio venta = costo * 1.5 -> 45 (intValue)
        assertEquals(45, producto.getPrecioVenta());

        verify(productoItemRepository).deleteByProductoIdProducto(1L);
        ArgumentCaptor<java.util.List> captor = ArgumentCaptor.forClass(java.util.List.class);
        verify(productoItemRepository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
    }

    @Test
    void ejecutarDebeFallarSiProductoNoEsElaboradoNiTransformado() {
        Producto producto = producto(1L, "Leche", TipoProducto.INSUMO);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        DefinirComposicionDto dto = new DefinirComposicionDto(1L, List.of(new InsumoRequeridoDto(2L, new BigDecimal("1.000"))));

        assertThrows(RuntimeException.class, () -> definirComposicionUseCase.ejecutar(dto));
    }

    @Test
    void ejecutarDebeFallarSiItemSinPrecio() {
        Producto producto = producto(1L, "Brownie", TipoProducto.ELABORADO);
        Item item = new Item();
        item.setIdItem(2L);
        item.setNombre("Harina");
        item.setPrecioUnitario(null);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(itemRepository.findAllById(List.of(2L))).thenReturn(List.of(item));

        DefinirComposicionDto dto = new DefinirComposicionDto(1L, List.of(new InsumoRequeridoDto(2L, new BigDecimal("1.000"))));

        assertThrows(RuntimeException.class, () -> definirComposicionUseCase.ejecutar(dto));
    }

    @Test
    void ejecutarDebeFallarSiProductoNoEncontrado() {
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());
        DefinirComposicionDto dto = new DefinirComposicionDto(1L, List.of(new InsumoRequeridoDto(2L, new BigDecimal("1.000"))));

        assertThrows(RuntimeException.class, () -> definirComposicionUseCase.ejecutar(dto));
    }

    private Producto producto(Long id, String nombre, TipoProducto tipo) {
        Producto producto = Producto.builder()
                .idProducto(id)
                .nombre(nombre)
                .tipo(tipo)
                .build();
        return producto;
    }
}
