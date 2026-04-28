package com.casualidad.casualidad_backend.inventario.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.casualidad.casualidad_backend.inventario.domain.model.MovimientoInventario;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    // CORREGIDO: Ahora retorna Page y recibe Pageable. 
    // Lleva countQuery porque usamos JOIN FETCH con @ManyToOne (Seguro).
    @Query(value = "SELECT m FROM MovimientoInventario m JOIN FETCH m.producto WHERE m.producto.idProducto = :idProducto",
           countQuery = "SELECT COUNT(m) FROM MovimientoInventario m WHERE m.producto.idProducto = :idProducto")
    Page<MovimientoInventario> findByProductoIdWithProducto(@Param("idProducto") Long idProducto, Pageable pageable);
}
