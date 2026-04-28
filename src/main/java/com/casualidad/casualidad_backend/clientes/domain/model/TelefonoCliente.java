package com.casualidad.casualidad_backend.clientes.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Index;
import jakarta.persistence.Entity;
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
import lombok.Setter;

@Getter @Setter 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor
@Entity
@Table(name = "telefonos_cliente", indexes = {
    @Index(name = "idx_telefono_numero", columnList = "numeroTelefono")
})
public class TelefonoCliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTelefono;

    @Column(nullable = false, length = 20)
    private String numeroTelefono;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;
}