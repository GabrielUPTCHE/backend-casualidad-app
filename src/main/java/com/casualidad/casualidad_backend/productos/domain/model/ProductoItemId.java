package com.casualidad.casualidad_backend.productos.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class ProductoItemId implements Serializable {
    @Column(name = "id_producto")
    private Long idProducto;

    @Column(name = "id_item")
    private Long idItem;
}