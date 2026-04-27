package com.casualidad.casualidad_backend.clientes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.casualidad.casualidad_backend.clientes.domain.model.TelefonoCliente;

import java.util.List;

@Repository
public interface TelefonoClienteRepository extends JpaRepository<TelefonoCliente, Long> {
    
    List<TelefonoCliente> findByNumeroTelefonoIn(List<String> numerosTelefono);

    @Query("SELECT t FROM TelefonoCliente t WHERE t.numeroTelefono IN :telefonos AND t.cliente.idCliente != :idCliente")
    List<TelefonoCliente> findTelefonosDeOtrosClientes(@Param("telefonos") List<String> telefonos, @Param("idCliente") Long idCliente);
}