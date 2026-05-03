package com.casualidad.casualidad_backend.pedidos.service;

import com.casualidad.casualidad_backend.clientes.domain.model.Cliente;
import com.casualidad.casualidad_backend.common.domain.enums.EstadoPedido;
import com.casualidad.casualidad_backend.common.domain.enums.MotivoMovimiento;
import com.casualidad.casualidad_backend.common.domain.enums.TipoMovimiento;
import com.casualidad.casualidad_backend.inventario.domain.model.MovimientoInventario;
import com.casualidad.casualidad_backend.inventario.repository.MovimientoInventarioRepository;
import com.casualidad.casualidad_backend.pedidos.domain.exception.StockInsuficienteException;
import com.casualidad.casualidad_backend.pedidos.domain.model.Pedido;
import com.casualidad.casualidad_backend.pedidos.repository.PedidoRepository;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;
import com.casualidad.casualidad_backend.productos.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivarProduccionPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    @Transactional
    public String ejecutar(Long idPedido) {
        // 1. Validar estado inicial
        Pedido pedido = obtenerPedidoValidado(idPedido);

        // 2. Calcular total de insumos requeridos usando Streams
        Map<Long, BigDecimal> requerimientos = calcularInsumosRequeridos(pedido);

        // 3. Cargar inventario involucrado (Evita N+1)
        Map<Long, Producto> inventario = obtenerInventario(requerimientos.keySet());

        // 4. Validar disponibilidad de stock
        validarDisponibilidad(requerimientos, inventario);

        // 5. Aplicar descuentos y registrar auditoría
        procesarDescuentoInventario(requerimientos, inventario, pedido.getIdPedido());

        // 6. Finalizar y generar código
        return formalizarPasoAProduccion(pedido);
    }

    // --- Métodos Privados Atómicos ---

 private Pedido obtenerPedidoValidado(Long idPedido) {
        //  Usamos tu método personalizado para asegurar que traiga los detalles 
        // y evitar problemas de Lazy Loading (Carga Perezosa).
        Pedido pedido = pedidoRepository.findByIdWithDetallesCompletos(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (pedido.getEstadoPedido() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("El pedido debe estar en estado PENDIENTE_ABONO para activarse.");
        }
        return pedido;
    }
private Map<Long, BigDecimal> calcularInsumosRequeridos(Pedido pedido) {
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            log.warn("El pedido {} no tiene detalles registrados. No se requiere inventario.", pedido.getIdPedido());
            return Map.of();
        }

        return pedido.getDetalles().stream()
                .flatMap(detalle -> {
                    Producto productoVendido = detalle.getProducto();
                    BigDecimal cantidadPedida = BigDecimal.valueOf(detalle.getCantidad());

                    // CASO 1: El producto TIENE una composición (Receta)
                    if (productoVendido.getComposicion() != null && !productoVendido.getComposicion().isEmpty()) {
                        return productoVendido.getComposicion().stream()
                                .map(receta -> Map.entry(
                                        receta.getInsumo().getIdProducto(),
                                        receta.getCantidadUsada().multiply(cantidadPedida)
                                ));
                    } 
                    // CASO 2: El producto NO tiene composición (Se vende directamente)
                    else {
                        return java.util.stream.Stream.of(Map.entry(
                                productoVendido.getIdProducto(),
                                cantidadPedida
                        ));
                    }
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        BigDecimal::add
                ));
    }
    /**
     * Busca y trae del repositorio los productos (insumos) involucrados en la producción del pedido,
     * mapeándolos por su ID para acceso rápido.
     * @param idsInsumos Conjunto de IDs productos (insumos).
     * @return Mapa de ID de insumo a su entidad Producto correspondiente.
     */

    private Map<Long, Producto> obtenerInventario(Set<Long> idsInsumos) {
        if (idsInsumos.isEmpty()) return Map.of();
        return productoRepository.findAllById(idsInsumos).stream()
                .collect(Collectors.toMap(Producto::getIdProducto, p -> p));
    }

    private void validarDisponibilidad(Map<Long, BigDecimal> requerimientos, Map<Long, Producto> inventario) {
        List<String> faltantes = requerimientos.entrySet().stream()
                .map(req -> evaluarDeficit(inventario.get(req.getKey()), req.getValue()))
                .filter(Objects::nonNull) // Filtramos los que SÍ tienen déficit
                .toList();

        if (!faltantes.isEmpty()) {
            log.warn("Intento de producción fallido por falta de stock. Faltantes: {}", faltantes);
            throw new StockInsuficienteException(faltantes);
        }
    }

    private String evaluarDeficit(Producto insumo, BigDecimal cantidadRequerida) {
        if (insumo == null) return "Insumo no encontrado en el sistema.";
        
        BigDecimal stockActual = insumo.getCantidad() != null ? insumo.getCantidad() : BigDecimal.ZERO;
        
        if (stockActual.compareTo(cantidadRequerida) < 0) {
            return String.format("Faltan %s unidades de '%s' (Requerido: %s, Actual: %s)",
                    cantidadRequerida.subtract(stockActual), insumo.getNombre(), cantidadRequerida, stockActual);
        }
        return null; // Retorna null si hay stock suficiente
    }

    private void procesarDescuentoInventario(Map<Long, BigDecimal> requerimientos, Map<Long, Producto> inventario, Long idPedido) {
        List<MovimientoInventario> movimientos = requerimientos.entrySet().stream()
                .map((Map.Entry<Long, BigDecimal> req) -> {
                    Producto insumo = inventario.get(req.getKey());
                    BigDecimal cantidadADescontar = req.getValue();
                    
                    BigDecimal stockAnterior = insumo.getCantidad();
                    BigDecimal stockNuevo = stockAnterior.subtract(cantidadADescontar);
                    
                    insumo.setCantidad(stockNuevo);
                    return MovimientoInventario.builder()
                            .producto(insumo)
                            .cantidad(cantidadADescontar.negate()) // Guardado como salida
                            .cantidadAnterior(stockAnterior)
                            .cantidadNueva(stockNuevo)
                            .tipoMovimiento(TipoMovimiento.SALIDA)
                            .motivo(MotivoMovimiento.CONSUMO)
                            .fechaMovimiento(LocalDateTime.now())
                            .build();
                })
                .toList();

        // Operaciones de BD en lote (Mejora radical de performance)
        productoRepository.saveAll(inventario.values());
        movimientoRepository.saveAll(movimientos);
    }

    private String formalizarPasoAProduccion(Pedido pedido) {
        String codigoGenerado = generarCodigoUnico(pedido.getCliente());
        pedido.setCodigoUnico(codigoGenerado);
        pedido.setEstadoPedido(EstadoPedido.EN_PRODUCCION);
        pedidoRepository.save(pedido);
        return codigoGenerado;
    }

    private String generarCodigoUnico(Cliente cliente) {
        String fechaStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String iniciales = obtenerIniciales(cliente.getNombre());
        
        LocalDateTime inicioDia = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime finDia = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Long pedidosHoy = pedidoRepository.countByCreadoEnBetween(inicioDia, finDia);
        
        return String.format("%s-%s-%02d", fechaStr, iniciales, pedidosHoy + 1);
    }

    private String obtenerIniciales(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) return "XX";
        String[] partes = nombreCompleto.trim().split("\\s+");
        StringBuilder iniciales = new StringBuilder();
        for (int i = 0; i < Math.min(2, partes.length); i++) {
            iniciales.append(partes[i].toUpperCase().charAt(0));
        }
        return iniciales.length() == 1 ? iniciales.append("X").toString() : iniciales.toString();
    }
}