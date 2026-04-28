package com.casualidad.casualidad_backend.pagos.service;

import com.casualidad.casualidad_backend.common.domain.enums.EstadoPedido;
import com.casualidad.casualidad_backend.pagos.dto.response.DetalleSaldoPendienteDto;
import com.casualidad.casualidad_backend.pagos.dto.response.ReporteSaldosPendientesResponseDto;
import com.casualidad.casualidad_backend.pedidos.domain.model.Pedido;
import com.casualidad.casualidad_backend.pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerarReporteSaldosUseCase {

    private final PedidoRepository pedidoRepository;

    @Transactional(readOnly = true)
    public ReporteSaldosPendientesResponseDto ejecutar() {
        // Definimos los estados que implican gestión de cobro previa a entrega
        List<EstadoPedido> estadosGestion = Arrays.asList(
                EstadoPedido.PENDIENTE, 
                EstadoPedido.EN_PRODUCCION
                // EstadoPedido.TERMINADO // Podrías agregarlo aquí
        );

        List<Pedido> pedidos = pedidoRepository.buscarSaldosPendientesPorEstados(estadosGestion);

        BigDecimal totalPorCobrar = pedidos.stream()
                .map(Pedido::getSaldoPendiente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DetalleSaldoPendienteDto> detalles = pedidos.stream()
                .map(p -> new DetalleSaldoPendienteDto(
                        p.getIdPedido(),
                        p.getCodigoUnico(),
                        p.getCliente().getNombre() ,
                        p.getFechaEntrega(),
                        p.getTotal(),
                        p.getSaldoPendiente()
                )).toList();

        return new ReporteSaldosPendientesResponseDto(
                totalPorCobrar,
                pedidos.size(),
                detalles
        );
    }
}