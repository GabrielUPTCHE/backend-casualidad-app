package com.casualidad.casualidad_backend.inventario.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.casualidad.casualidad_backend.common.domain.enums.MotivoMovimiento;
import com.casualidad.casualidad_backend.common.domain.enums.TipoMovimiento;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movimientos_inventario")
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMovimiento;

    @Column(precision = 10, scale = 3, nullable = false)
    private BigDecimal cantidad; // Este sigue siendo el delta (diferencia)

    @Column(precision = 10, scale = 3)
    private BigDecimal cantidadAnterior; // CA 2: Auditoría

    @Column(precision = 10, scale = 3)
    private BigDecimal cantidadNueva;    // CA 2: Auditoría

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipoMovimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MotivoMovimiento motivo;

    @Column(columnDefinition = "TEXT")
    private String comentario; // Para guardar el "Por qué" específico (CA 1)

    @Column(nullable = false)
    private LocalDateTime fechaMovimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;
}