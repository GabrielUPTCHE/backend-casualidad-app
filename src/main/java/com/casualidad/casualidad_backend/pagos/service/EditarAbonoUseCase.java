package com.casualidad.casualidad_backend.pagos.service;

import com.casualidad.casualidad_backend.auth.entity.Usuario;
import com.casualidad.casualidad_backend.common.domain.enums.TipoPago;
import com.casualidad.casualidad_backend.pagos.domain.model.AuditoriaPago;
import com.casualidad.casualidad_backend.pagos.domain.model.Pago;
import com.casualidad.casualidad_backend.pagos.dto.request.EditarAbonoRequestDto;
import com.casualidad.casualidad_backend.pagos.dto.response.AbonoResponseDto;
import com.casualidad.casualidad_backend.pagos.repository.AuditoriaPagoRepository;
import com.casualidad.casualidad_backend.pagos.repository.PagoRepository;
import com.casualidad.casualidad_backend.pedidos.domain.model.Pedido;
import com.casualidad.casualidad_backend.pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EditarAbonoUseCase {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;
    private final AuditoriaPagoRepository auditoriaPagoRepository;

    @Transactional
    public AbonoResponseDto ejecutar(Long idPago, EditarAbonoRequestDto request, Usuario admin) {
        
        // 1. Buscar el pago existente
        Pago pagoActual = pagoRepository.findById(idPago)
                .orElseThrow(() -> new IllegalArgumentException("Abono no encontrado con ID: " + idPago));
        
        Pedido pedido = pagoActual.getPedido();

        // 2. Calcular el saldo "base" (como si este pago no existiera)
        BigDecimal saldoSinEstePago = pedido.getSaldoPendiente().add(pagoActual.getMonto());

        // 3. CA 5: Validar que el nuevo monto no supere el saldo base
        if (request.monto().compareTo(saldoSinEstePago) > 0) {
            throw new IllegalStateException("El nuevo monto supera el saldo disponible del pedido ($" + saldoSinEstePago + ")");
        }

        // 4. CA 3: Recalcular el nuevo saldo pendiente del pedido
        BigDecimal nuevoSaldoPendiente = saldoSinEstePago.subtract(request.monto());
        pedido.setSaldoPendiente(nuevoSaldoPendiente);

        // 5. CA 1: Determinar el nuevo Tipo de Pago (si cubre todo es PAGO_SALDO, si no, ABONO)
        TipoPago nuevoTipoPago = nuevoSaldoPendiente.compareTo(BigDecimal.ZERO) == 0 
                ? TipoPago.PAGO_SALDO 
                : TipoPago.ABONO;

        // 6. Actualizar la entidad Pago
        BigDecimal montoAnterior = pagoActual.getMonto(); // Lo guardamos para la auditoría
        pagoActual.setMonto(request.monto());
        pagoActual.setMetodoPago(request.metodoPago());
        pagoActual.setTipoPago(nuevoTipoPago);
        // pagoActual.setReferencia(request.referenciaComprobante()); // Si tienes este campo

        // Guardar cambios en BD
        pedidoRepository.save(pedido);
        pagoRepository.save(pagoActual);

        // 7. CA 4: Registro de Auditoría
        String detalleAuditoria = String.format("Edición de abono. Monto anterior: $%s | Nuevo monto: $%s | Método: %s", 
                montoAnterior, request.monto(), request.metodoPago());
                
        AuditoriaPago auditoria = AuditoriaPago.builder()
                .idPago(pagoActual.getIdPago())
                .idPedido(pedido.getIdPedido())
                .accion("EDITAR")
                .detalle(detalleAuditoria)
                .usuarioResponsable(admin.getNombre()) 
                .fecha(LocalDateTime.now())
                .build();
        auditoriaPagoRepository.save(auditoria);

        // 8. Retornar DTO
        return new AbonoResponseDto(
                pagoActual.getIdPago(),
                pagoActual.getMonto(),
                pagoActual.getMetodoPago(),
                pagoActual.getTipoPago(),
                pagoActual.getFechaPago(),
                request.referenciaComprobante(),
                pedido.getSaldoPendiente()
        );
    }
}