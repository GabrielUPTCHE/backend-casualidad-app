package com.casualidad.casualidad_backend.productos.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "composicion_producto", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_producto_padre", "id_insumo"})
})
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ComposicionProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idComposicion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto_padre", nullable = false)
    private Producto productoPadre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_insumo", nullable = false)
    private Producto insumo;

    @Column(precision = 10, scale = 3, nullable = false)
    private BigDecimal cantidadUsada;
}