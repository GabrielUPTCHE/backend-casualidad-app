package com.casualidad.casualidad_backend.productos.domain.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "producto_item")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductoItem {
    @EmbeddedId
    private ProductoItemId id = new ProductoItemId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idProducto")
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idItem")
    @JoinColumn(name = "id_item")
    private Item item;

    @Column(precision = 10, scale = 3, nullable = false)
    private BigDecimal cantidadRequerida;
}
