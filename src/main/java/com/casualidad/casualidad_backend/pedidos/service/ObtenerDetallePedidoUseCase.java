package com.casualidad.casualidad_backend.pedidos.service;

import com.casualidad.casualidad_backend.common.domain.dtos.response.PageResponse;
import com.casualidad.casualidad_backend.pagos.domain.model.Pago;
import com.casualidad.casualidad_backend.pagos.repository.PagoRepository;
import com.casualidad.casualidad_backend.pedidos.domain.model.Pedido;
import com.casualidad.casualidad_backend.pedidos.dto.response.*;
import com.casualidad.casualidad_backend.pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ObtenerDetallePedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;

    @Transactional(readOnly = true)
    public PedidoDetalleCompletoDto ejecutar(Long idPedido, Pageable pageableAbonos) {
        
        // 1. Buscar el pedido principal
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado con ID: " + idPedido));

        // 2. Mapear Cliente
        ClienteBreveDto clienteDto = new ClienteBreveDto(
                pedido.getCliente().getIdCliente(),
                pedido.getCliente().getNombre(),
                pedido.getCliente().getTelefonos().get(0).getNumeroTelefono()
        );

        // 3. Mapear Productos
        List<DetalleProductoPedidoDto> productosDto = pedido.getDetalles().stream()
                .map(d -> new DetalleProductoPedidoDto(
                        d.getIdDetalle(),
                        d.getProducto().getNombre(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getSubtotal(),
                        d.getObservaciones()
                )).toList();

        // 4. Buscar y Mapear Abonos (Paginados)
        Page<Pago> paginaPagos = pagoRepository.findByPedido_IdPedido(idPedido, pageableAbonos);
        
        List<AbonoDto> abonosDto = paginaPagos.getContent().stream()
                .map(p -> new AbonoDto(
                        p.getIdPago(),
                        p.getMonto(),
                        p.getFechaPago(),
                        p.getMetodoPago().toString() // O p.getMetodoPago().name() si es Enum
                )).toList();

        PageResponse<AbonoDto> historialAbonosPage = PageResponse.<AbonoDto>builder()
                .pageNumber(paginaPagos.getNumber())
                .pageSize(paginaPagos.getSize())
                .totalElements(paginaPagos.getTotalElements())
                .totalPages(paginaPagos.getTotalPages())
                .data(abonosDto)
                .build();

        // 5. Ensamblar DTO Final
        return new PedidoDetalleCompletoDto(
                pedido.getIdPedido(),
                pedido.getCodigoUnico(),
                pedido.getEstadoPedido(),
                pedido.getFechaEntrega(),
                pedido.getTotal(),
                pedido.getSaldoPendiente(),
                clienteDto,
                productosDto,
                historialAbonosPage
        );
    }
}