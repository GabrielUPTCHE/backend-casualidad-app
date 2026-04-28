package com.casualidad.casualidad_backend.productos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.casualidad.casualidad_backend.common.domain.dtos.response.PageResponse;
import com.casualidad.casualidad_backend.common.domain.enums.TipoProducto;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.domain.model.UnidadMedida;
import com.casualidad.casualidad_backend.productos.dto.response.InventarioItemDto;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ConsultarInventarioUseCaseTest {

    @Mock private ProductoRepository productoRepository;

    @InjectMocks private ConsultarInventarioUseCase consultarInventarioUseCase;

    @Test
    void ejecutarDebeMapearInventarioCorrectamente() {
        Producto producto = Producto.builder()
                .idProducto(1L)
                .nombre("Harina")
                .tipo(TipoProducto.INSUMO)
                .cantidad(new BigDecimal("3.000"))
                .stockMinimo(new BigDecimal("5.000"))
                .unidadMedida(new UnidadMedida(1L, "kg"))
                .build();

        when(productoRepository.buscarInventarioConFiltros(any(), any(), any())).thenReturn(new PageImpl<>(List.of(producto), PageRequest.of(0, 10), 1));

        PageResponse<InventarioItemDto> response = consultarInventarioUseCase.ejecutar(null, null, PageRequest.of(0, 10));

        assertEquals(1L, response.getTotalElements());
        assertEquals(1, response.getData().size());
        assertEquals(true, response.getData().get(0).stockBajo());
    }
}