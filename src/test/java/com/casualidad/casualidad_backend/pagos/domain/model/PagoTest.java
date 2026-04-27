package com.casualidad.casualidad_backend.pagos.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.casualidad.casualidad_backend.common.domain.enums.MetodoPago;
import com.casualidad.casualidad_backend.common.domain.enums.TipoPago;
import com.casualidad.casualidad_backend.pedidos.domain.model.Pedido;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

class PagoTest {

    @Test
    void debePermitirAsignarSusCamposPrincipales() {
        Pago pago = new Pago();
        pago.setMonto(new BigDecimal("150.00"));
        pago.setMetodoPago(MetodoPago.EFECTIVO);
        pago.setFechaPago(LocalDateTime.of(2026, 4, 27, 10, 0));
        pago.setTipoPago(TipoPago.ABONO);
        pago.setPedido(new Pedido());

        assertEquals(new BigDecimal("150.00"), pago.getMonto());
        assertEquals(MetodoPago.EFECTIVO, pago.getMetodoPago());
        assertEquals(TipoPago.ABONO, pago.getTipoPago());
    }

    @Test
    void serializaYDeserializaPago() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Pago pago = new Pago();
        pago.setMonto(new BigDecimal("99.99"));
        pago.setMetodoPago(com.casualidad.casualidad_backend.common.domain.enums.MetodoPago.EFECTIVO);
        pago.setTipoPago(com.casualidad.casualidad_backend.common.domain.enums.TipoPago.ABONO);
        pago.setFechaPago(LocalDateTime.of(2026, 4, 27, 10, 0));

        String json = mapper.writeValueAsString(pago);
        Pago leido = mapper.readValue(json, Pago.class);

        assertEquals(pago.getMonto(), leido.getMonto());
        assertEquals(pago.getMetodoPago(), leido.getMetodoPago());
        assertEquals(pago.getTipoPago(), leido.getTipoPago());
    }
}