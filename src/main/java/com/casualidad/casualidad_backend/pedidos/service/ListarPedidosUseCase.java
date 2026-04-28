package com.casualidad.casualidad_backend.pedidos.service;

import com.casualidad.casualidad_backend.common.domain.dtos.response.PageResponse;
import com.casualidad.casualidad_backend.pedidos.domain.model.Pedido;
import com.casualidad.casualidad_backend.pedidos.dto.request.FiltroPedidoDto;
import com.casualidad.casualidad_backend.pedidos.dto.response.PedidoResumenDto;
import com.casualidad.casualidad_backend.pedidos.repository.PedidoRepository;
import com.casualidad.casualidad_backend.pedidos.repository.PedidoSpecification;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarPedidosUseCase {

    private final PedidoRepository pedidoRepository;

    @Transactional(readOnly = true)
    public PageResponse<PedidoResumenDto> ejecutar(FiltroPedidoDto filtro, Pageable pageable) {
        
        // Validación del Escenario de Error (Fechas Incoherentes)
        if (filtro.fechaInicio() != null && filtro.fechaFin() != null && filtro.fechaInicio().isAfter(filtro.fechaFin())) {
            throw new IllegalArgumentException("Rango de fechas inválido");
        }

        // Consultar a BD con filtros y paginación
        Page<Pedido> paginaPedidos = pedidoRepository.findAll(PedidoSpecification.conFiltros(filtro), pageable);

        // Mapear Entidad a DTO de Resumen
        List<PedidoResumenDto> datos = paginaPedidos.getContent().stream()
                .map(p -> new PedidoResumenDto(
                        p.getIdPedido(),
                        p.getCodigoUnico(),
                        p.getCliente().getNombre(), // Ajusta si tu entidad cliente tiene apellido: p.getCliente().getNombre() + " " + p.getCliente().getApellido()
                        p.getEstadoPedido(),
                        p.getFechaEntrega(),
                        p.getTotal(),
                        p.getSaldoPendiente()
                ))
                .toList();

        // Retornar en el formato estándar del proyecto (CA 3)
        return PageResponse.<PedidoResumenDto>builder()
                .pageNumber(paginaPedidos.getNumber())
                .pageSize(paginaPedidos.getSize())
                .totalElements(paginaPedidos.getTotalElements())
                .totalPages(paginaPedidos.getTotalPages())
                .data(datos)
                .build();
    }
}