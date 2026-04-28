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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgregarInsumosProductoUseCase {

    private final ProductoRepository productoRepository;

    @Transactional
    public ComposicionResponseDto ejecutar(Long idProductoPadre, List<InsumoComposicionDto> nuevosInsumosDto) {
        
        // 1. Validar que la petición no venga vacía
        if (nuevosInsumosDto == null || nuevosInsumosDto.isEmpty()) {
            throw new ComposicionVaciaException(); 
        }

        // 2. Buscar producto padre y validar que permita tener insumos
        Producto padre = productoRepository.findById(idProductoPadre)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto padre no encontrado con ID: " + idProductoPadre));

        validarEsElaboradoOTransformado(padre);

        // 3. Optimización: Buscar todos los insumos nuevos en BD de un solo golpe (Evita N+1)
        List<Long> idsInsumos = nuevosInsumosDto.stream().map(InsumoComposicionDto::idInsumo).toList();
        Map<Long, Producto> mapaInsumos = productoRepository.findAllById(idsInsumos).stream()
                .collect(Collectors.toMap(Producto::getIdProducto, p -> p));

        // 4. Procesar y agregar cada insumo nuevo
        for (InsumoComposicionDto dto : nuevosInsumosDto) {
            Producto insumoNuevo = mapaInsumos.get(dto.idInsumo());
            
            if (insumoNuevo == null) {
                throw new RecursoNoEncontradoException("Insumo no encontrado en BD: " + dto.idInsumo());
            }

            // Validar que el insumo tenga precio (CA - Error de Cálculo)
            if (insumoNuevo.getPrecioCompra() == null || insumoNuevo.getPrecioCompra() <= 0) {
                throw new CostoIncalculableException(insumoNuevo.getNombre());
            }

            // Validar que no se repitan insumos en la misma receta (Evita violar el UniqueConstraint de BD)
            boolean yaExisteEnReceta = padre.getComposicion().stream()
                    .anyMatch(c -> c.getInsumo().getIdProducto().equals(insumoNuevo.getIdProducto()));
            
            if (yaExisteEnReceta) {
                throw new IllegalArgumentException("El insumo '" + insumoNuevo.getNombre() + "' ya existe en la composición de este producto.");
            }

            // Crear la relación y AÑADIRLA a la lista existente (NO hacemos .clear())
            ComposicionProducto nuevaRelacion = ComposicionProducto.builder()
                    .productoPadre(padre)
                    .insumo(insumoNuevo)
                    .cantidadUsada(dto.cantidadUsada())
                    .build();

            padre.getComposicion().add(nuevaRelacion);
        }

        // 5. Recalcular el costo total de producción (Suma los insumos viejos + los recién agregados)
        BigDecimal costoTotal = recalcularCostoProduccion(padre.getComposicion());
        
        // Asignamos el nuevo costo total al producto padre
        padre.setPrecioCompra(BigDecimal.valueOf(costoTotal.doubleValue()).intValue()); 
        
        // 6. Persistir cambios
        productoRepository.save(padre);

        // 7. Retornar el DTO de respuesta mapeando la receta completa
        return construirRespuesta(padre, costoTotal);
    }

    // --- Métodos Privados Atómicos ---

    private void validarEsElaboradoOTransformado(Producto producto) {
        if (producto.getTipo() != TipoProducto.ELABORADO && producto.getTipo() != TipoProducto.TRANSFORMADO) {
            throw new TipoProductoInvalidoException("Solo los productos ELABORADOS o TRANSFORMADOS pueden tener insumos asociados.");
        }
    }

    private BigDecimal recalcularCostoProduccion(List<ComposicionProducto> composicion) {
        return composicion.stream()
                .map(c -> BigDecimal.valueOf(c.getInsumo().getPrecioCompra()).multiply(c.getCantidadUsada()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ComposicionResponseDto construirRespuesta(Producto padre, BigDecimal costoTotal) {
        List<ComposicionResponseDto.DetalleInsumoDto> detalles = padre.getComposicion().stream()
                .map(c -> ComposicionResponseDto.DetalleInsumoDto.builder()
                        .idInsumo(c.getInsumo().getIdProducto())
                        .nombre(c.getInsumo().getNombre())
                        .cantidadUsada(c.getCantidadUsada())
                        .precioUnitarioCompra(c.getInsumo().getPrecioCompra())
                        .subtotal(BigDecimal.valueOf(c.getInsumo().getPrecioCompra()).multiply(c.getCantidadUsada()))
                        .build())
                .toList();

        return ComposicionResponseDto.builder()
                .idProductoPadre(padre.getIdProducto())
                .nombreProducto(padre.getNombre())
                .costoTotalProduccion(costoTotal)
                .insumos(detalles)
                .build();
    }
}
