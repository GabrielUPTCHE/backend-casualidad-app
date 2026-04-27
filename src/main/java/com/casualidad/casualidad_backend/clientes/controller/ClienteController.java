package com.casualidad.casualidad_backend.clientes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.casualidad.casualidad_backend.clientes.dto.request.ClienteRequestDto;
import com.casualidad.casualidad_backend.clientes.dto.response.ClienteListadoDto;
import com.casualidad.casualidad_backend.clientes.dto.response.ClienteResponseDto;
import com.casualidad.casualidad_backend.clientes.service.ActualizarClienteUseCase;
import com.casualidad.casualidad_backend.clientes.service.CrearClienteService;
import com.casualidad.casualidad_backend.clientes.service.EliminarClienteUseCase;
import com.casualidad.casualidad_backend.clientes.service.ListarClientesUseCase;
import com.casualidad.casualidad_backend.common.domain.dtos.response.ApiResponse;
import com.casualidad.casualidad_backend.common.domain.dtos.response.PageResponse;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final CrearClienteService clienteService;
    private final ActualizarClienteUseCase actualizarClienteUseCase;
    private final EliminarClienteUseCase eliminarClienteUseCase;
    private final ListarClientesUseCase listarClientesUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<ClienteResponseDto>> registrar(@Valid @RequestBody ClienteRequestDto dto) {
        ClienteResponseDto data = clienteService.registrarCliente(dto);
        
        ApiResponse<ClienteResponseDto> response = ApiResponse.<ClienteResponseDto>builder()
                .message("Cliente registrado exitosamente")
                .code(HttpStatus.CREATED.value())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponseDto>> actualizar(
            @PathVariable Long id, 
            @Valid @RequestBody ClienteRequestDto dto) {
        
        ClienteResponseDto data = actualizarClienteUseCase.ejecutar(id, dto);
        
        ApiResponse<ClienteResponseDto> response = ApiResponse.<ClienteResponseDto>builder()
                .message("Datos actualizados correctamente")
                .code(HttpStatus.OK.value())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        // El servicio decide si es borrado lógico o físico y devuelve el mensaje adecuado
        String mensaje = eliminarClienteUseCase.ejecutar(id);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message(mensaje)
                .code(HttpStatus.OK.value())
                .data(null)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ClienteListadoDto>>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filtro) {
        
        PageResponse<ClienteListadoDto> data = listarClientesUseCase.ejecutar(page, size, filtro);
        
        // CA 3: Lógica dinámica para el mensaje
        String mensaje = data.getTotalElements() == 0 
                ? "No se encontraron clientes con ese criterio de búsqueda"
                : "Clientes listados exitosamente";

        ApiResponse<PageResponse<ClienteListadoDto>> response = ApiResponse.<PageResponse<ClienteListadoDto>>builder()
                .message(mensaje)
                .code(HttpStatus.OK.value())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
