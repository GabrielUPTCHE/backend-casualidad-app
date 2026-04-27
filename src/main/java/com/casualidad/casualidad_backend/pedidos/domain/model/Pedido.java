package com.casualidad.casualidad_backend.pedidos.domain.model;

import com.casualidad.casualidad_backend.auth.entity.Usuario;
import com.casualidad.casualidad_backend.clientes.domain.model.Cliente;
import com.casualidad.casualidad_backend.common.domain.enums.EstadoPedido;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPedido;

    @Column(length = 20, unique = true)
    private String codigoUnico; // Añadido para el CA 3

    @Column(nullable = false)
    private LocalDateTime creadoEn;

    @Enumerated(EnumType.STRING) // En el diagrama: 0 (PENDIENTE), 1 (EN_PRODUCCION)...
    @Column(nullable = false, length = 30)
    private EstadoPedido estadoPedido;

    @Column(nullable = false)
    private LocalDate fechaEntrega;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal saldoPendiente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductoPedido> detalles = new ArrayList<>();

    public void addDetalle(ProductoPedido detalle) {
        detalles.add(detalle);
        detalle.setPedido(this);
    }
}