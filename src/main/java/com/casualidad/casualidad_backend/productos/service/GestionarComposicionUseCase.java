package com.casualidad.casualidad_backend.productos.service;

import com.casualidad.casualidad_backend.clientes.domain.exception.RecursoNoEncontradoException;
import com.casualidad.casualidad_backend.common.domain.enums.TipoProducto;
import com.casualidad.casualidad_backend.productos.domain.exception.*;
import com.casualidad.casualidad_backend.productos.domain.model.ComposicionProducto;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.dto.InsumoComposicionDto;
import com.casualidad.casualidad_backend.productos.dto.response.ComposicionResponseDto;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GestionarComposicionUseCase {

    private final ProductoRepository productoRepository;

    @Transactional
    public ComposicionResponseDto ejecutar(Long idProductoPadre, List<InsumoComposicionDto> insumosDto) {
        if (insumosDto == null || insumosDto.isEmpty()) {
            throw new ComposicionVaciaException(); // CA - Error Composición Vacía
        }

        Producto padre = buscarProducto(idProductoPadre);
        validarEsElaboradoOTransformado(padre);

        // Limpiamos la composición actual (Orphan Removal se encargará de borrar en BD)
        padre.getComposicion().clear();

        List<ComposicionProducto> nuevaComposicion = insumosDto.stream()
                .map(dto -> mapearAComposicion(padre, dto))
                .toList();

        padre.getComposicion().addAll(nuevaComposicion);
        productoRepository.save(padre);

        // CA 4 y 5: Si el producto está en producción, deberíamos disparar un evento de dominio aquí.
        // Ej: eventPublisher.publishEvent(new ComposicionEditadaEvent(padre.getId()));

        return calcularYConstruirRespuesta(padre, nuevaComposicion);
    }

    // --- Métodos Privados Atómicos ---

    private Producto buscarProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado."));
    }

    private void validarEsElaboradoOTransformado(Producto producto) {
        if (producto.getTipo() != TipoProducto.ELABORADO && producto.getTipo() != TipoProducto.TRANSFORMADO) {
            throw new TipoProductoInvalidoException("Solo los productos ELABORADOS o TRANSFORMADOS pueden tener insumos asociados.");
        }
    }

    private ComposicionProducto mapearAComposicion(Producto padre, InsumoComposicionDto dto) {
        Producto insumo = buscarProducto(dto.idInsumo());
        
        if (insumo.getPrecioCompra() == null || insumo.getPrecioCompra() <= 0) {
            throw new CostoIncalculableException(insumo.getNombre()); // CA - Error de Cálculo
        }

        return ComposicionProducto.builder()
                .productoPadre(padre)
                .insumo(insumo)
                .cantidadUsada(dto.cantidadUsada())
                .build();
    }

    private ComposicionResponseDto calcularYConstruirRespuesta(Producto padre, List<ComposicionProducto> composiciones) {
        BigDecimal costoTotal = BigDecimal.ZERO;
        List<ComposicionResponseDto.DetalleInsumoDto> detalles = new java.util.ArrayList<>();

        for (ComposicionProducto comp : composiciones) {
            BigDecimal precioInsumo = BigDecimal.valueOf(comp.getInsumo().getPrecioCompra());
            BigDecimal subtotal = precioInsumo.multiply(comp.getCantidadUsada());
            costoTotal = costoTotal.add(subtotal);

            detalles.add(ComposicionResponseDto.DetalleInsumoDto.builder()
                    .idInsumo(comp.getInsumo().getIdProducto())
                    .nombre(comp.getInsumo().getNombre())
                    .cantidadUsada(comp.getCantidadUsada())
                    .precioUnitarioCompra(comp.getInsumo().getPrecioCompra())
                    .subtotal(subtotal)
                    .build());
        }

        return ComposicionResponseDto.builder()
                .idProductoPadre(padre.getIdProducto())
                .nombreProducto(padre.getNombre())
                .costoTotalProduccion(costoTotal)
                .insumos(detalles)
                .build();
    }
}
