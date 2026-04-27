package com.casualidad.casualidad_backend.productos.controller;


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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final RegistrarProductoUseCase registrarProductoUseCase;
    private final GestionarComposicionUseCase gestionarComposicionUseCase;
    private final EditarProductoUseCase editarProductoUseCase;
    private final ConsultarInventarioUseCase consultarInventarioUseCase;
    private final AgregarInsumosProductoUseCase agregarInsumosProductoUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> registrar(
            @Valid @RequestBody ProductoRequestDto dto) {
        
        Long idProducto = registrarProductoUseCase.ejecutar(dto);
        
        ApiResponse<Long> response = ApiResponse.<Long>builder()
                .message("Producto registrado exitosamente")
                .code(HttpStatus.CREATED.value())
                .data(idProducto) // Retornamos el ID para que el Front pueda redirigir o confirmar
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/composicion")
    public ResponseEntity<ApiResponse<ComposicionResponseDto>> asociarInsumos(
            @PathVariable Long id,
            @RequestBody List<@Valid InsumoComposicionDto> insumos) {
        
        ComposicionResponseDto resultado = gestionarComposicionUseCase.ejecutar(id, insumos);
        
        ApiResponse<ComposicionResponseDto> response = ApiResponse.<ComposicionResponseDto>builder()
                .message("Composición guardada y costo calculado exitosamente")
                .code(HttpStatus.OK.value())
                .data(resultado)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> editarProducto(
            @PathVariable Long id,
            @Valid @RequestBody EditarProductoDto dto) {
        
        editarProductoUseCase.ejecutar(id, dto);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Producto actualizado exitosamente")
                .code(HttpStatus.OK.value())
                .data(null)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InventarioItemDto>>> consultarInventario(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) TipoProducto tipo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombre,asc") String[] sort) {

        // Construir la paginación de Spring
        org.springframework.data.domain.Sort.Direction direction = 
                sort[1].equalsIgnoreCase("desc") ? 
                org.springframework.data.domain.Sort.Direction.DESC : 
                org.springframework.data.domain.Sort.Direction.ASC;
                
        org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, sort[0]));

        // El UseCase ahora devuelve tu PageResponse
        PageResponse<InventarioItemDto> resultado = consultarInventarioUseCase.ejecutar(nombre, tipo, pageable);

        ApiResponse<PageResponse<InventarioItemDto>> response = ApiResponse.<PageResponse<InventarioItemDto>>builder()
                .message("Inventario consultado exitosamente")
                .code(HttpStatus.OK.value())
                .data(resultado)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * HU 11: Agrega nuevos insumos a la ficha técnica (composición) de un producto.
     * Endpoint: POST /api/v1/productos/{idProducto}/composicion
     * * @param idProductoPadre El ID del producto ELABORADO o TRANSFORMADO
     * @param nuevosInsumos Lista de DTOs con los insumos y cantidades a agregar
     * @return DTO con la composición actualizada y el nuevo costo total calculado
     */
    @PostMapping("/{idProducto}/composicion")
    public ResponseEntity<ComposicionResponseDto> agregarInsumosAProducto(
            @PathVariable("idProducto") Long idProductoPadre,
            @Valid @NotEmpty(message = "La lista de insumos no puede estar vacía") 
            @RequestBody List<InsumoComposicionDto> nuevosInsumos) {

        ComposicionResponseDto respuesta = agregarInsumosProductoUseCase.ejecutar(idProductoPadre, nuevosInsumos);

        // Retornamos 201 CREATED porque estamos creando nuevos registros en la tabla pivote
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}