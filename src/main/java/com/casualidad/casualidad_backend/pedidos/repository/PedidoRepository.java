package com.casualidad.casualidad_backend.pedidos.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.casualidad.casualidad_backend.common.domain.enums.EstadoPedido;
import com.casualidad.casualidad_backend.pedidos.domain.model.Pedido;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // (Único) Factura completa, no se pagina porque es un solo ID
    @Query("SELECT p FROM Pedido p " +
           "JOIN FETCH p.cliente " +
           "JOIN FETCH p.usuario " +
           "LEFT JOIN FETCH p.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "WHERE p.idPedido = :idPedido")
    Optional<Pedido> findByIdWithDetallesCompletos(@Param("idPedido") Long idPedido);

    @Query(value = "SELECT p FROM Pedido p JOIN FETCH p.cliente WHERE p.estadoPedido = :estado",
           countQuery = "SELECT COUNT(p) FROM Pedido p WHERE p.estadoPedido = :estado")
    Page<Pedido> findByEstadoWithCliente(@Param("estado") EstadoPedido estado, Pageable pageable);

    long countByClienteIdCliente(Long idCliente);

    long countByCreadoEnBetween(LocalDateTime inicio, LocalDateTime fin);
}