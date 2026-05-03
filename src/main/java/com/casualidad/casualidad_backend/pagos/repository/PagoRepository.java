package com.casualidad.casualidad_backend.pagos.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.casualidad.casualidad_backend.pagos.domain.model.Pago;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    @Query(value = "SELECT p FROM Pago p JOIN FETCH p.pedido WHERE p.pedido.idPedido = :idPedido",
           countQuery = "SELECT COUNT(p) FROM Pago p WHERE p.pedido.idPedido = :idPedido")
    Page<Pago> findByPedidoId(@Param("idPedido") Long idPedido, Pageable pageable);

    Page<Pago> findByPedido_IdPedido(Long idPedido, Pageable pageable);
    @Query("SELECT p FROM Pago p " +
           "JOIN p.pedido ped " +
           "WHERE p.fechaPago BETWEEN :inicio AND :fin " +
           "AND ped.estadoPedido <> 'CANCELADO' " +
           "ORDER BY p.fechaPago DESC")
    List<Pago> buscarIngresosPorPeriodo(
        @Param("inicio") LocalDateTime inicio, 
        @Param("fin") LocalDateTime fin
    );

    @Query(value = "SELECT p FROM Pago p JOIN FETCH p.pedido JOIN FETCH p.pedido.cliente",
           countQuery = "SELECT count(p) FROM Pago p")
    Page<Pago> findAllWithPedido(Pageable pageable);
}