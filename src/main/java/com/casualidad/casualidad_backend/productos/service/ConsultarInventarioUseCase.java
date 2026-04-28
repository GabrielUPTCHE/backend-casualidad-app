package com.casualidad.casualidad_backend.productos.service;

import com.casualidad.casualidad_backend.common.domain.dtos.response.PageResponse;
import com.casualidad.casualidad_backend.common.domain.enums.TipoProducto;
import com.casualidad.casualidad_backend.productos.dto.response.InventarioItemDto;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsultarInventarioUseCase {

    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public PageResponse<InventarioItemDto> ejecutar(String nombre, TipoProducto tipo, Pageable pageable) {
        
        // 1. Obtenemos el Page nativo de Spring Data
        Page<InventarioItemDto> paginaSpring = productoRepository.buscarInventarioConFiltros(nombre, tipo, pageable)
                .map(producto -> new InventarioItemDto(
                        producto.getIdProducto(),
                        producto.getNombre(),
                        producto.getTipo(),
                        producto.getUnidadMedida().getNombre(),
                        producto.getCantidad(),
                        producto.getCantidad().compareTo(producto.getStockMinimo()) <= 0 
                ));

        // 2. Lo mapeamos a tu objeto personalizado
        return PageResponse.<InventarioItemDto>builder()
                .pageNumber(paginaSpring.getNumber())
                .pageSize(paginaSpring.getSize())
                .totalElements(paginaSpring.getTotalElements())
                .totalPages(paginaSpring.getTotalPages())
                .data(paginaSpring.getContent())
                .build();
    }
}