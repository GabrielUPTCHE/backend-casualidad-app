package com.casualidad.casualidad_backend.pagos.dto.request;
import com.casualidad.casualidad_backend.common.domain.enums.MetodoPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record RegistrarAbonoRequestDto(
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    BigDecimal monto,

    // CA 2: El enum MetodoPago valida automáticamente que solo sea EFECTIVO o TRANSFERENCIA
    @NotNull(message = "El método de pago es obligatorio")
    MetodoPago metodoPago,

    // La HU menciona "Imagen del comprobante (opcional)". 
    // Por ahora lo manejamos como una URL o texto de referencia.
    String referenciaComprobante 
) {}
