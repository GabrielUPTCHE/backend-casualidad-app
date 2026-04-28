package com.casualidad.casualidad_backend.pagos.service;

import com.casualidad.casualidad_backend.auth.entity.Usuario;
import com.casualidad.casualidad_backend.pagos.domain.model.AuditoriaPago;
import com.casualidad.casualidad_backend.pagos.domain.model.Pago;
import com.casualidad.casualidad_backend.pagos.dto.response.SaldoActualizadoResponseDto;
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
public class EliminarAbonoUseCase {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;
    private final AuditoriaPagoRepository auditoriaPagoRepository;

    @Transactional
    public SaldoActualizadoResponseDto ejecutar(Long idPago, Usuario admin) {
        
        // 1. Buscar el pago existente
        Pago pagoActual = pagoRepository.findById(idPago)
                .orElseThrow(() -> new IllegalArgumentException("Abono no encontrado con ID: " + idPago));
        
        Pedido pedido = pagoActual.getPedido();
        BigDecimal montoAEliminar = pagoActual.getMonto();

        // 2. CA 3: Restaurar el saldo pendiente sumando el monto del abono eliminado
        BigDecimal nuevoSaldoPendiente = pedido.getSaldoPendiente().add(montoAEliminar);
        pedido.setSaldoPendiente(nuevoSaldoPendiente);
        
        pedidoRepository.save(pedido);

        // 3. CA 4: Registro de Auditoría ANTES de borrar el registro
        String detalleAuditoria = String.format("Eliminación de abono. Se reversó un pago de $%s vía %s.", 
                montoAEliminar, pagoActual.getMetodoPago());
                
        AuditoriaPago auditoria = AuditoriaPago.builder()
                .idPago(pagoActual.getIdPago()) // Guardamos el ID por histórico, aunque ya no exista en la tabla pagos
                .idPedido(pedido.getIdPedido())
                .accion("ELIMINAR")
                .detalle(detalleAuditoria)
                .usuarioResponsable(admin.getNombre()) 
                .fecha(LocalDateTime.now())
                .build();
        auditoriaPagoRepository.save(auditoria);

        // 4. Eliminar físicamente el abono de la base de datos
        pagoRepository.delete(pagoActual);

        // 5. Retornar el nuevo estado financiero
        return new SaldoActualizadoResponseDto(
                pedido.getSaldoPendiente(),
                "Abono eliminado correctamente. El saldo ha sido reversado."
        );
    }
}
