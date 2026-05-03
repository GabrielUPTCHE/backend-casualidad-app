package com.casualidad.casualidad_backend.pagos.service;

import com.casualidad.casualidad_backend.common.domain.dtos.response.PageResponse;
import com.casualidad.casualidad_backend.pagos.domain.model.Pago;
import com.casualidad.casualidad_backend.pagos.dto.response.PagoDTO;
import com.casualidad.casualidad_backend.pagos.repository.PagoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;

    @Transactional(readOnly = true)
    public PageResponse<PagoDTO> listarPagosPaginados(int page, int size) {
        // Ordenamos por fecha de pago descendente por defecto
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaPago").descending());
        
        Page<Pago> pagosPage = pagoRepository.findAllWithPedido(pageable);

        List<PagoDTO> data = pagosPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return PageResponse.<PagoDTO>builder()
                .pageNumber(pagosPage.getNumber())
                .pageSize(pagosPage.getSize())
                .totalElements(pagosPage.getTotalElements())
                .totalPages(pagosPage.getTotalPages())
                .data(data)
                .build();
    }

    private PagoDTO mapToDTO(Pago pago) {
        return PagoDTO.builder()
                .idPago(pago.getIdPago())
                .monto(pago.getMonto())
                .metodoPago(pago.getMetodoPago().name())
                .fechaPago(pago.getFechaPago())
                .tipoPago(pago.getTipoPago().name())
                .idPedido(pago.getPedido().getIdPedido())
                .codigoPedido(pago.getPedido().getCodigoUnico())
                .estadoPedido(pago.getPedido().getEstadoPedido().name())
                .saldoPendiente(pago.getPedido().getSaldoPendiente())
                .nombreCliente(pago.getPedido().getCliente().getNombre())
                .build();
    }
}
