package com.casualidad.casualidad_backend.auth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.casualidad.casualidad_backend.auth.dto.AuthDTOs.LoginRequest;
import com.casualidad.casualidad_backend.auth.dto.AuthDTOs.LoginResponse;
import com.casualidad.casualidad_backend.auth.dto.AuthDTOs.UsuarioInfo;
import com.casualidad.casualidad_backend.auth.dto.RegisterRequest;
import com.casualidad.casualidad_backend.auth.dto.RegisterResponse;
import com.casualidad.casualidad_backend.auth.security.AuthService;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

	@Mock
	private AuthService authService;

	@InjectMocks
	private AuthController authController;

	@Test
	void loginDebeDelegarEnAuthServiceYRetornarOk() {
		LoginRequest request = new LoginRequest("usuario@casualidad.com", "secreta");
		LoginResponse responseEsperado = new LoginResponse(
			"access-token",
			"refresh-token",
			new UsuarioInfo("7", "Usuario Test", "usuario@casualidad.com", "USUARIO")
		);

		when(authService.login(request)).thenReturn(responseEsperado);

		ResponseEntity<LoginResponse> response = authController.login(request);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(responseEsperado, response.getBody());
		verify(authService).login(request);
	}

	@Test
	void recuperarPasswordDebeRetornarMensajeDeConfirmacion() {
		ResponseEntity<String> response = authController.recuperarPassword("usuario@casualidad.com");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Si el correo existe, se ha enviado un enlace de recuperación", response.getBody());
	}

	@Test
	void recuperarCorreoDebeRetornarMensajeDeInicioDeProceso() {
		ResponseEntity<String> response = authController.recuperarCorreo("123");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Proceso de recuperación de correo iniciado", response.getBody());
	}

	@Test
	void registerDebeDelegarEnAuthServiceYRetornarOk() {
		RegisterRequest request = new RegisterRequest();
		request.setNombre("Nuevo Usuario");
		request.setCorreo("nuevo@casualidad.com");
		request.setContraseña("123456");
		request.setRol("CLIENTE");

		RegisterResponse responseEsperado = new RegisterResponse(
			"access-token",
			"refresh-token",
			"Nuevo Usuario"
		);

		when(authService.register(request)).thenReturn(responseEsperado);

		ResponseEntity<RegisterResponse> response = authController.register(request);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(responseEsperado, response.getBody());
		verify(authService).register(request);
	}
}
