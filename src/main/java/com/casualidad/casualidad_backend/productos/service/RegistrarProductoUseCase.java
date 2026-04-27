package com.casualidad.casualidad_backend.productos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.casualidad.casualidad_backend.common.domain.enums.TipoProducto;
import com.casualidad.casualidad_backend.productos.domain.exception.ProductoDeInsumoException;
import com.casualidad.casualidad_backend.productos.domain.exception.ProductoDeReventaException;
import com.casualidad.casualidad_backend.productos.domain.exception.UnidadDeMedidaException;
import com.casualidad.casualidad_backend.productos.domain.exception.YaExisteProductoException;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.domain.model.UnidadMedida;
import com.casualidad.casualidad_backend.productos.dto.request.ProductoRequestDto;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;
import com.casualidad.casualidad_backend.productos.repository.UnidadMedidaRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RegistrarProductoUseCase {

    private final ProductoRepository productoRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;

    @Transactional
    public Long ejecutar(ProductoRequestDto dto) {
        validarNombreUnico(dto.nombre());
        validarReglasPorTipo(dto);
        
        UnidadMedida unidad = resolverUnidadMedida(dto);
        Producto producto = construirProducto(dto, unidad);
        
        return productoRepository.save(producto).getIdProducto();
    }

    // --- Métodos Privados (Core de las reglas de negocio) ---

    private void validarNombreUnico(String nombre) {
        if (productoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new YaExisteProductoException("Ya existe un producto con este nombre en el sistema.");
        }
    }

    private void validarReglasPorTipo(ProductoRequestDto dto) {
        if (dto.tipo() == TipoProducto.REVENTA && dto.precioVenta() == null) {
            throw new ProductoDeReventaException("Los productos de reventa requieren un precio de venta para ser añadidos a los pedidos.");
        }

        if (dto.tipo() == TipoProducto.ELABORADO && !productoRepository.existsByTipo(TipoProducto.INSUMO)) {
            throw new ProductoDeInsumoException("No hay insumos registrados. Debe crear insumos antes de registrar un producto elaborado.");
        }
    }

    private UnidadMedida resolverUnidadMedida(ProductoRequestDto dto) {
        if (dto.idUnidadMedida() != null) {
            return unidadMedidaRepository.findById(dto.idUnidadMedida())
                    .orElseThrow(() -> new UnidadDeMedidaException("Unidad de medida no encontrada."));
        }
        
        if (dto.nuevaUnidadMedida() == null || dto.nuevaUnidadMedida().isBlank()) {
            throw new UnidadDeMedidaException("Debe seleccionar una unidad o crear una nueva.");
        }

        // Si mandaron un string para crear una nueva (CA 5)
        return unidadMedidaRepository.findByNombreIgnoreCase(dto.nuevaUnidadMedida().trim())
                .orElseGet(() -> unidadMedidaRepository.save(
                    UnidadMedida.builder().nombre(dto.nuevaUnidadMedida().trim()).build()
                ));
    }

    private Producto construirProducto(ProductoRequestDto dto, UnidadMedida unidad) {
        // Aseguramos que el porcentaje sobrante se guarde SOLO si es INSUMO
        BigDecimal porcentaje = (dto.tipo() == TipoProducto.INSUMO) ? dto.porcentajeSobrante() : null;
        
        // CA 1 y CA 6: El default 0 lo maneja el @Builder.Default si viene null
        BigDecimal stockMin = dto.stockMinimo() != null ? dto.stockMinimo() : BigDecimal.ZERO;

        return Producto.builder()
                .nombre(dto.nombre().trim())
                .tipo(dto.tipo())
                .unidadMedida(unidad)
                .cantidad(dto.cantidad() != null ? dto.cantidad() : BigDecimal.ZERO)
                .stockMinimo(stockMin)
                .precioCompra(dto.precioCompra())
                .precioVenta(dto.precioVenta())
                .porcentajeSobrante(porcentaje)
                .build();
    }
}
