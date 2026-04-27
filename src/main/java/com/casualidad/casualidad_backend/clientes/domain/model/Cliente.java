package com.casualidad.casualidad_backend.clientes.domain.model;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.Index;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "clientes", indexes = {
    @Index(name = "idx_cliente_nombre", columnList = "nombre"),
    @Index(name = "idx_cliente_activo", columnList = "activo")
})
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCliente;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(length = 100)
    private String direccion;

    private Boolean activo = true;

    // Relación bidireccional útil para traer al cliente con sus teléfonos
    @Builder.Default
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20) 
    private List<TelefonoCliente> telefonos = new ArrayList<>();
}