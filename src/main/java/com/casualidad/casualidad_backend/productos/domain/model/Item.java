package com.casualidad.casualidad_backend.productos.domain.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "items")
public class Item {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idItem;
    @Column(length = 100, nullable = false)
    private String nombre;
    @Column(length = 20)
    private String unidadMedida;
    @Column(precision = 10, scale = 3)
    private BigDecimal cantidadMinima;
    @Column(precision = 12, scale = 2)
    private BigDecimal precioUnitario;
}