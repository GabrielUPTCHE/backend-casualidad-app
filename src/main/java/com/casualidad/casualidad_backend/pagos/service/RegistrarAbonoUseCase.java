package com.casualidad.casualidad_backend.pagos.service;

import com.casualidad.casualidad_backend.auth.entity.Usuario; // Ajusta según tu ruta
import com.casualidad.casualidad_backend.common.domain.enums.TipoPago;
import com.casualidad.casualidad_backend.pagos.domain.model.AuditoriaPago;
import com.casualidad.casualidad_backend.pagos.domain.model.Pago;
import com.casualidad.casualidad_backend.pagos.dto.request.RegistrarAbonoRequestDto;
import com.casualidad.casualidad_backend.pagos.dto.response.AbonoResponseDto;
import com.casualidad.casualidad_backend.pagos.repository.AuditoriaPagoRepository;
import com.casualidad.casualidad_backend.pagos.repository.PagoRepository;
import com.casualidad.casualidad_backend.pedidos.domain.model.Pedido;
import com.casualidad.casualidad_backend.pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegistrarAbonoUseCase {

    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;
    private final AuditoriaPagoRepository auditoriaPagoRepository;

    @Transactional
    public AbonoResponseDto ejecutar(Long idPedido, RegistrarAbonoRequestDto request, Usuario admin) {
        
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        // CA 5: Validar que el monto no supere el saldo pendiente (Escenario de Error)
        if (request.monto().compareTo(pedido.getSaldoPendiente()) > 0) {
            throw new IllegalStateException("El monto no puede superar el saldo pendiente ($" + pedido.getSaldoPendiente() + ")");
        }

        // Determinar el Tipo de Pago (CA 1 implícito: ¿Abona o liquida la deuda?)
        TipoPago tipoCalculado = request.monto().compareTo(pedido.getSaldoPendiente()) == 0 
                ? TipoPago.PAGO_SALDO 
                : TipoPago.ABONO;

        // Construir la entidad Pago
        Pago nuevoPago = new Pago();
        nuevoPago.setPedido(pedido);
        nuevoPago.setMonto(request.monto());
        nuevoPago.setMetodoPago(request.metodoPago());
        nuevoPago.setTipoPago(tipoCalculado);
        nuevoPago.setFechaPago(LocalDateTime.now());
        // Asumiendo que agregas este campo a tu entidad Pago para guardar la URL de la imagen
        // nuevoPago.setReferencia(request.referenciaComprobante()); 
        
        pagoRepository.save(nuevoPago);

        // Actualizar el saldo del pedido (CA 1)
        pedido.setSaldoPendiente(pedido.getSaldoPendiente().subtract(request.monto()));
        pedidoRepository.save(pedido);

        // CA 4: Registro de Auditoría
        AuditoriaPago auditoria = AuditoriaPago.builder()
                .idPago(nuevoPago.getIdPago())
                .idPedido(pedido.getIdPedido())
                .accion("CREAR")
                .detalle("Registro de abono mediante " + request.metodoPago())
                .usuarioResponsable(admin.getNombre()) // O getUsername(), dependiendo de tu entidad Usuario
                .fecha(LocalDateTime.now())
                .build();
        auditoriaPagoRepository.save(auditoria);

        // Retornar respuesta mapeada
        return new AbonoResponseDto(
                nuevoPago.getIdPago(),
                nuevoPago.getMonto(),
                nuevoPago.getMetodoPago(),
                nuevoPago.getTipoPago(),
                nuevoPago.getFechaPago(),
                request.referenciaComprobante(),
                pedido.getSaldoPendiente()
        );
    }
}