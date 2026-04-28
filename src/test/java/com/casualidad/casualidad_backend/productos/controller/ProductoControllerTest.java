package com.casualidad.casualidad_backend.productos.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.casualidad.casualidad_backend.common.domain.dtos.response.ApiResponse;
import com.casualidad.casualidad_backend.common.domain.dtos.response.PageResponse;
import com.casualidad.casualidad_backend.common.domain.enums.TipoProducto;
import com.casualidad.casualidad_backend.productos.dto.InsumoComposicionDto;
import com.casualidad.casualidad_backend.productos.dto.request.EditarProductoDto;
import com.casualidad.casualidad_backend.productos.dto.request.ProductoRequestDto;
import com.casualidad.casualidad_backend.productos.dto.response.ComposicionResponseDto;
import com.casualidad.casualidad_backend.productos.dto.response.InventarioItemDto;
import com.casualidad.casualidad_backend.productos.service.AgregarInsumosProductoUseCase;
import com.casualidad.casualidad_backend.productos.service.ConsultarInventarioUseCase;
import com.casualidad.casualidad_backend.productos.service.EditarProductoUseCase;
import com.casualidad.casualidad_backend.productos.service.GestionarComposicionUseCase;
import com.casualidad.casualidad_backend.productos.service.RegistrarProductoUseCase;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    @Mock
    private RegistrarProductoUseCase registrarProductoUseCase;

    @Mock
    private GestionarComposicionUseCase gestionarComposicionUseCase;

    @Mock
    private EditarProductoUseCase editarProductoUseCase;

    @Mock
    private ConsultarInventarioUseCase consultarInventarioUseCase;

    @Mock
    private AgregarInsumosProductoUseCase agregarInsumosProductoUseCase;

    @InjectMocks
    private ProductoController productoController;

    @Test
    void registrarDebeDelegarYRetornarCreatedConId() {
	ProductoRequestDto request = new ProductoRequestDto(
		"Harina",
		TipoProducto.INSUMO,
		null,
		"Kg",
		new BigDecimal("10"),
		new BigDecimal("2"),
		1500,
		2500,
		new BigDecimal("0")
	);

	when(registrarProductoUseCase.ejecutar(request)).thenReturn(99L);

	ResponseEntity<ApiResponse<Long>> response = productoController.registrar(request);

	assertEquals(HttpStatus.CREATED, response.getStatusCode());
	assertEquals("Producto registrado exitosamente", response.getBody().getMessage());
	assertEquals(HttpStatus.CREATED.value(), response.getBody().getCode());
	assertEquals(99L, response.getBody().getData());
	verify(registrarProductoUseCase).ejecutar(request);
    }

    @Test
    void asociarInsumosDebeDelegarYRetornarOkConComposicion() {
	List<InsumoComposicionDto> insumos = List.of(
		new InsumoComposicionDto(1L, new BigDecimal("2.5"))
	);
	ComposicionResponseDto respuestaEsperada = ComposicionResponseDto.builder()
		.idProductoPadre(10L)
		.nombreProducto("Pan integral")
		.costoTotalProduccion(new BigDecimal("3750"))
		.insumos(List.of())
		.build();

	when(gestionarComposicionUseCase.ejecutar(10L, insumos)).thenReturn(respuestaEsperada);

	ResponseEntity<ApiResponse<ComposicionResponseDto>> response = productoController.asociarInsumos(10L, insumos);

	assertEquals(HttpStatus.OK, response.getStatusCode());
	assertEquals("Composición guardada y costo calculado exitosamente", response.getBody().getMessage());
	assertEquals(HttpStatus.OK.value(), response.getBody().getCode());
	assertEquals(respuestaEsperada, response.getBody().getData());
	verify(gestionarComposicionUseCase).ejecutar(10L, insumos);
    }

    @Test
    void editarProductoDebeDelegarYRetornarOk() {
	EditarProductoDto request = new EditarProductoDto(
		"Harina refinada",
		null,
		"Kg",
		new BigDecimal("5"),
		1200,
		1800,
		new BigDecimal("0")
	);

	ResponseEntity<ApiResponse<Void>> response = productoController.editarProducto(7L, request);

	assertEquals(HttpStatus.OK, response.getStatusCode());
	assertEquals("Producto actualizado exitosamente", response.getBody().getMessage());
	assertEquals(HttpStatus.OK.value(), response.getBody().getCode());
	assertEquals(null, response.getBody().getData());
	verify(editarProductoUseCase).ejecutar(7L, request);
    }

    @Test
    void consultarInventarioDebeConstruirPageableConOrdenDescendente() {
	InventarioItemDto item = new InventarioItemDto(
		3L,
		"Azucar",
		TipoProducto.INSUMO,
		"Kg",
		new BigDecimal("12.5"),
		false
	);
	PageResponse<InventarioItemDto> respuestaEsperada = PageResponse.<InventarioItemDto>builder()
		.pageNumber(0)
		.pageSize(10)
		.totalElements(1L)
		.totalPages(1)
		.data(List.of(item))
		.build();

	when(consultarInventarioUseCase.ejecutar(eq("azu"), eq(TipoProducto.INSUMO), any(Pageable.class)))
		.thenReturn(respuestaEsperada);

	ResponseEntity<ApiResponse<PageResponse<InventarioItemDto>>> response = productoController.consultarInventario(
		"azu",
		TipoProducto.INSUMO,
		0,
		10,
		new String[] {"precioCompra", "desc"}
	);

	ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
	verify(consultarInventarioUseCase).ejecutar(eq("azu"), eq(TipoProducto.INSUMO), pageableCaptor.capture());

	Pageable pageable = pageableCaptor.getValue();
	assertEquals(0, pageable.getPageNumber());
	assertEquals(10, pageable.getPageSize());
	assertTrue(pageable.getSort().getOrderFor("precioCompra").isDescending());
	assertEquals(HttpStatus.OK, response.getStatusCode());
	assertEquals("Inventario consultado exitosamente", response.getBody().getMessage());
	assertEquals(HttpStatus.OK.value(), response.getBody().getCode());
	assertEquals(respuestaEsperada, response.getBody().getData());
    }

    @Test
    void agregarInsumosAProductoDebeRetornarCreatedConRespuestaDelUseCase() {
	List<InsumoComposicionDto> nuevosInsumos = List.of(
		new InsumoComposicionDto(8L, new BigDecimal("1.25"))
	);
	ComposicionResponseDto respuestaEsperada = ComposicionResponseDto.builder()
		.idProductoPadre(15L)
		.nombreProducto("Torta")
		.costoTotalProduccion(new BigDecimal("2500"))
		.insumos(List.of())
		.build();

	when(agregarInsumosProductoUseCase.ejecutar(15L, nuevosInsumos)).thenReturn(respuestaEsperada);

	ResponseEntity<ComposicionResponseDto> response = productoController.agregarInsumosAProducto(15L, nuevosInsumos);

	assertEquals(HttpStatus.CREATED, response.getStatusCode());
	assertEquals(respuestaEsperada, response.getBody());
	verify(agregarInsumosProductoUseCase).ejecutar(15L, nuevosInsumos);
    }
}
