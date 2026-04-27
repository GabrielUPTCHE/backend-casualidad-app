package com.casualidad.casualidad_backend.clientes.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.casualidad.casualidad_backend.clientes.domain.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // OPTIMIZADO: Adiós DISTINCT y LEFT JOIN. Hola EXISTS.
    @Query("SELECT c FROM Cliente c " +
           "WHERE c.activo = true " +
           "AND (:filtro IS NULL OR :filtro = '' " +
           "OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
           "OR EXISTS (SELECT t FROM TelefonoCliente t WHERE t.cliente = c AND t.numeroTelefono LIKE CONCAT('%', :filtro, '%')))")
    Page<Cliente> buscarActivosPorFiltro(@Param("filtro") String filtro, Pageable pageable);

}