package com.casualidad.casualidad_backend.productos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.casualidad.casualidad_backend.common.domain.enums.TipoProducto;
import com.casualidad.casualidad_backend.productos.domain.model.Item;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.domain.model.ProductoItem;
import com.casualidad.casualidad_backend.productos.domain.model.ProductoItemId;
import com.casualidad.casualidad_backend.productos.dto.request.DefinirComposicionDto;
import com.casualidad.casualidad_backend.productos.repository.ItemRepository;
import com.casualidad.casualidad_backend.productos.repository.ProductoItemRepository;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefinirComposicionUseCase {

    private final ProductoRepository productoRepository;
    private final ItemRepository itemRepository;
    private final ProductoItemRepository productoItemRepository;

    @Transactional
    public BigDecimal ejecutar(DefinirComposicionDto dto) {
        Producto producto = productoRepository.findById(dto.idProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Validar que sea Elaborado o Transformado (CA1)
        if (producto.getTipo() != TipoProducto.ELABORADO && producto.getTipo() != TipoProducto.TRANSFORMADO) {
            throw new RuntimeException("Solo se pueden asociar insumos a productos Elaborados o Transformados");
        }

        // Limpiar composición anterior si existe (para reemplazo completo)
        productoItemRepository.deleteByProductoIdProducto(producto.getIdProducto());

        // Obtener IDs de los items requeridos para un Fetch en lote (Evitar N+1)
        List<Long> idsItems = dto.insumos().stream().map(i -> i.idItem()).toList();
        Map<Long, Item> itemsMap = itemRepository.findAllById(idsItems).stream()
                .collect(Collectors.toMap(Item::getIdItem, item -> item));

        // Construir la nueva composición y calcular el costo
        List<ProductoItem> nuevaComposicion = dto.insumos().stream().map(insumoDto -> {
            Item item = itemsMap.get(insumoDto.idItem());
            if (item == null) throw new RuntimeException("Insumo no encontrado: " + insumoDto.idItem());
            
            // Escenario (-): Error de Cálculo por falta de precio en el maestro
            if (item.getPrecioUnitario() == null || item.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("No se puede calcular el costo total porque faltan precios en el insumo: " + item.getNombre());
            }

            return ProductoItem.builder()
                    .id(new ProductoItemId(producto.getIdProducto(), item.getIdItem()))
                    .producto(producto)
                    .item(item)
                    .cantidadRequerida(insumoDto.cantidadRequerida())
                    .build();
        }).toList();

        productoItemRepository.saveAll(nuevaComposicion);

        // CA2: Recalcular costo total usando Streams (Precio Insumo * Cantidad)
        BigDecimal costoTotalProduccion = nuevaComposicion.stream()
                .map(pi -> pi.getItem().getPrecioUnitario().multiply(pi.getCantidadRequerida()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Actualizar el costo histórico del producto (según tu diagrama)
        producto.setPrecioVenta( costoTotalProduccion.multiply(BigDecimal.valueOf(1.5)).intValue());
        
        return costoTotalProduccion;
    }
}