package com.casualidad.casualidad_backend.pagos.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PagoDTO {
    private Long idPago;
    private BigDecimal monto;
    private String metodoPago;
    private LocalDateTime fechaPago;
    private String tipoPago;
    private BigDecimal saldoPendiente;
    private String nombreCliente;
    
    // Información del pedido asociado
    private Long idPedido;
    private String codigoPedido;
    private String estadoPedido; 
}