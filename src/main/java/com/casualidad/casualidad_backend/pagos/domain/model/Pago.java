package com.casualidad.casualidad_backend.pagos.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.casualidad.casualidad_backend.common.domain.enums.MetodoPago;
import com.casualidad.casualidad_backend.common.domain.enums.TipoPago;
import com.casualidad.casualidad_backend.pedidos.domain.model.Pedido;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "pagos")
public class Pago {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPago;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    private LocalDateTime fechaPago;

    @Enumerated(EnumType.STRING)
    private TipoPago tipoPago;

    // Relación LAZY hacia Pedidos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;
}