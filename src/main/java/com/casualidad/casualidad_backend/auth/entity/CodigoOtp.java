package com.casualidad.casualidad_backend.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.casualidad.casualidad_backend.common.domain.enums.PropositoOtp;

@Entity
@Table(name = "codigos_otp")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CodigoOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 6)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropositoOtp proposito;

    @Column(nullable = false)
    private Integer intentosFallidos;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    private LocalDateTime bloqueadoHasta;

    // Campo extra: Si el propósito es CAMBIO_CORREO_NUEVO, aquí guardamos el correo al que quiere cambiar
    private String valorFuturo;

    public boolean estaBloqueado() {
        return bloqueadoHasta != null && LocalDateTime.now().isBefore(bloqueadoHasta);
    }
    
    public boolean estaExpirado() {
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }
}
