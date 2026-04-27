package com.casualidad.casualidad_backend.productos.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "unidades_medida")
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class UnidadMedida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUnidad;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;
}