package com.casualidad.casualidad_backend.clientes.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.casualidad.casualidad_backend.clientes.dto.request.ClienteRequestDto;
import com.casualidad.casualidad_backend.clientes.dto.response.ClienteListadoDto;
import com.casualidad.casualidad_backend.clientes.dto.response.ClienteResponseDto;
import com.casualidad.casualidad_backend.clientes.service.ActualizarClienteUseCase;
import com.casualidad.casualidad_backend.clientes.service.CrearClienteService;
import com.casualidad.casualidad_backend.clientes.service.EliminarClienteUseCase;
import com.casualidad.casualidad_backend.clientes.service.ListarClientesUseCase;
import com.casualidad.casualidad_backend.common.domain.dtos.response.ApiResponse;
import com.casualidad.casualidad_backend.common.domain.dtos.response.PageResponse;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    @Mock
    private CrearClienteService clienteService;

    @Mock
    private ActualizarClienteUseCase actualizarClienteUseCase;

    @Mock
    private EliminarClienteUseCase eliminarClienteUseCase;

    @Mock
    private ListarClientesUseCase listarClientesUseCase;

    @InjectMocks
    private ClienteController clienteController;

    @Test
    void registrarDebeDelegarYRetornarCreatedConRespuestaEnvuelta() {
	ClienteRequestDto request = new ClienteRequestDto(
		"Juan Perez",
		List.of("3001234567"),
		"Calle 123"
	);
	ClienteResponseDto respuestaEsperada = new ClienteResponseDto(
		10L,
		"Juan Perez",
		"Calle 123",
		List.of("3001234567")
	);

	when(clienteService.registrarCliente(request)).thenReturn(respuestaEsperada);

	ResponseEntity<ApiResponse<ClienteResponseDto>> response = clienteController.registrar(request);

	assertEquals(HttpStatus.CREATED, response.getStatusCode());
	assertEquals("Cliente registrado exitosamente", response.getBody().getMessage());
	assertEquals(HttpStatus.CREATED.value(), response.getBody().getCode());
	assertEquals(respuestaEsperada, response.getBody().getData());
	verify(clienteService).registrarCliente(request);
    }

    @Test
    void actualizarDebeDelegarYRetornarOkConRespuestaEnvuelta() {
	ClienteRequestDto request = new ClienteRequestDto(
		"Juan Perez Actualizado",
		List.of("3001234567", "3017654321"),
		"Carrera 45"
	);
	ClienteResponseDto respuestaEsperada = new ClienteResponseDto(
		10L,
		"Juan Perez Actualizado",
		"Carrera 45",
		List.of("3001234567", "3017654321")
	);

	when(actualizarClienteUseCase.ejecutar(10L, request)).thenReturn(respuestaEsperada);

	ResponseEntity<ApiResponse<ClienteResponseDto>> response = clienteController.actualizar(10L, request);

	assertEquals(HttpStatus.OK, response.getStatusCode());
	assertEquals("Datos actualizados correctamente", response.getBody().getMessage());
	assertEquals(HttpStatus.OK.value(), response.getBody().getCode());
	assertEquals(respuestaEsperada, response.getBody().getData());
	verify(actualizarClienteUseCase).ejecutar(10L, request);
    }

    @Test
    void eliminarDebeDelegarYRetornarOkConMensajeDelUseCase() {
	when(eliminarClienteUseCase.ejecutar(15L)).thenReturn("Cliente eliminado correctamente");

	ResponseEntity<ApiResponse<Void>> response = clienteController.eliminar(15L);

	assertEquals(HttpStatus.OK, response.getStatusCode());
	assertEquals("Cliente eliminado correctamente", response.getBody().getMessage());
	assertEquals(HttpStatus.OK.value(), response.getBody().getCode());
	assertEquals(null, response.getBody().getData());
	verify(eliminarClienteUseCase).ejecutar(15L);
    }

    @Test
    void listarDebeRetornarMensajeDeExitoCuandoHayResultados() {
	ClienteListadoDto cliente = ClienteListadoDto.builder()
		.idCliente(1L)
		.nombre("Juan Perez")
		.direccion("Calle 123")
		.telefonos(List.of("3001234567"))
		.build();
	PageResponse<ClienteListadoDto> respuestaEsperada = PageResponse.<ClienteListadoDto>builder()
		.pageNumber(0)
		.pageSize(10)
		.totalElements(1L)
		.totalPages(1)
		.data(List.of(cliente))
		.build();

	when(listarClientesUseCase.ejecutar(0, 10, "Juan")).thenReturn(respuestaEsperada);

	ResponseEntity<ApiResponse<PageResponse<ClienteListadoDto>>> response = clienteController.listar(0, 10, "Juan");

	assertEquals(HttpStatus.OK, response.getStatusCode());
	assertEquals("Clientes listados exitosamente", response.getBody().getMessage());
	assertEquals(HttpStatus.OK.value(), response.getBody().getCode());
	assertEquals(respuestaEsperada, response.getBody().getData());
	verify(listarClientesUseCase).ejecutar(0, 10, "Juan");
    }

    @Test
    void listarDebeRetornarMensajeSinResultadosCuandoLaPaginaVieneVacia() {
	PageResponse<ClienteListadoDto> respuestaEsperada = PageResponse.<ClienteListadoDto>builder()
		.pageNumber(0)
		.pageSize(10)
		.totalElements(0L)
		.totalPages(0)
		.data(List.of())
		.build();

	when(listarClientesUseCase.ejecutar(0, 10, null)).thenReturn(respuestaEsperada);

	ResponseEntity<ApiResponse<PageResponse<ClienteListadoDto>>> response = clienteController.listar(0, 10, null);

	assertEquals(HttpStatus.OK, response.getStatusCode());
	assertEquals("No se encontraron clientes con ese criterio de búsqueda", response.getBody().getMessage());
	assertEquals(HttpStatus.OK.value(), response.getBody().getCode());
	assertEquals(respuestaEsperada, response.getBody().getData());
	verify(listarClientesUseCase).ejecutar(0, 10, null);
    }
}
