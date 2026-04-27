package com.casualidad.casualidad_backend.pagos.repository;

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
}