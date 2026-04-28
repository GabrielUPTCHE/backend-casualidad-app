package com.casualidad.casualidad_backend.pagos.domain.model;

import java.time.LocalDateTime;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "auditoria_pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAuditoria;

    @Column(nullable = false)
    private Long idPago;

    @Column(nullable = false)
    private Long idPedido;

    @Column(nullable = false, length = 50)
    private String accion; // Ej: "CREAR"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String detalle;

    @Column(nullable = false, length = 100)
    private String usuarioResponsable;

    @Column(nullable = false)
    private LocalDateTime fecha;
}