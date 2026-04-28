package com.casualidad.casualidad_backend.productos.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.casualidad.casualidad_backend.common.domain.enums.TipoProducto;

@Entity
@Table(name = "productos", indexes = {
    @Index(name = "idx_producto_nombre", columnList = "nombre")
})
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoProducto tipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_unidad", nullable = false)
    private UnidadMedida unidadMedida;

    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal cantidad = BigDecimal.ZERO;

    @Builder.Default
    @Column(precision = 7, scale = 2, nullable = false)
    private BigDecimal stockMinimo = BigDecimal.ZERO; // CA 1

    private Integer precioCompra;
    private Integer precioVenta;

    @Column(precision = 5, scale = 2)
    private BigDecimal porcentajeSobrante;

    @Builder.Default
    @OneToMany(mappedBy = "productoPadre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComposicionProducto> composicion = new ArrayList<>();
}