package com.casualidad.casualidad_backend.productos.service;

import com.casualidad.casualidad_backend.productos.domain.exception.YaExisteProductoException;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.domain.model.UnidadMedida;
import com.casualidad.casualidad_backend.productos.dto.request.EditarProductoDto;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;
import com.casualidad.casualidad_backend.productos.repository.UnidadMedidaRepository;
import com.casualidad.casualidad_backend.productos.domain.exception.ProductoDeReventaException;
import com.casualidad.casualidad_backend.productos.domain.exception.UnidadDeMedidaException;
import com.casualidad.casualidad_backend.common.domain.enums.TipoProducto;
import com.casualidad.casualidad_backend.inventario.domain.exception.ProductoNoExisteException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class EditarProductoUseCase {

    private final ProductoRepository productoRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;

    @Transactional
    public void ejecutar(Long idProducto, EditarProductoDto dto) {
        // Escenario (-): Producto inexistente
        Producto productoActual = productoRepository.findById(idProducto)
                .orElseThrow(ProductoNoExisteException::new);

        validarNombreUnicoEnEdicion(dto.nombre(), idProducto);
        validarReglasPorTipoExistente(productoActual.getTipo(), dto);

        UnidadMedida unidad = resolverUnidadMedida(dto, productoActual.getUnidadMedida());

        actualizarDatos(productoActual, dto, unidad);
        
        productoRepository.save(productoActual);
    }

    // --- Métodos Privados ---

    private void validarNombreUnicoEnEdicion(String nombre, Long idProducto) {
        if (productoRepository.existsByNombreIgnoreCaseAndIdProductoNot(nombre.trim(), idProducto)) {
            throw new YaExisteProductoException("Ya existe un producto con este nombre en el sistema.");
        }
    }

    private void validarReglasPorTipoExistente(TipoProducto tipoActual, EditarProductoDto dto) {
        if (tipoActual == TipoProducto.REVENTA && dto.precioVenta() == null) {
            throw new ProductoDeReventaException("Los productos de reventa requieren un precio de venta para ser añadidos a los pedidos.");
        }
    }

    private UnidadMedida resolverUnidadMedida(EditarProductoDto dto, UnidadMedida unidadActual) {
        // Si no mandan nada de unidad, mantenemos la actual
        if (dto.idUnidadMedida() == null && (dto.nuevaUnidadMedida() == null || dto.nuevaUnidadMedida().isBlank())) {
            return unidadActual;
        }

        if (dto.idUnidadMedida() != null) {
            return unidadMedidaRepository.findById(dto.idUnidadMedida())
                    .orElseThrow(() -> new UnidadDeMedidaException("Unidad de medida no encontrada."));
        }

        return unidadMedidaRepository.findByNombreIgnoreCase(dto.nuevaUnidadMedida().trim())
                .orElseGet(() -> unidadMedidaRepository.save(
                        UnidadMedida.builder().nombre(dto.nuevaUnidadMedida().trim()).build()
                ));
    }

    private void actualizarDatos(Producto producto, EditarProductoDto dto, UnidadMedida unidad) {
        // Nota experta: NUNCA tocamos producto.setCantidad() ni producto.setTipo() aquí
        
        producto.setNombre(dto.nombre().trim());
        producto.setUnidadMedida(unidad);
        
        if (dto.stockMinimo() != null) {
            producto.setStockMinimo(dto.stockMinimo());
        }
        
        producto.setPrecioCompra(dto.precioCompra());
        producto.setPrecioVenta(dto.precioVenta());

        if (producto.getTipo() == TipoProducto.INSUMO) {
            producto.setPorcentajeSobrante(dto.porcentajeSobrante());
        }
    }
}