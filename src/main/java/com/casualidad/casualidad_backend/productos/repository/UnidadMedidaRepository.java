package com.casualidad.casualidad_backend.productos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.casualidad.casualidad_backend.productos.domain.model.UnidadMedida;

public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, Long> {
    Optional<UnidadMedida> findByNombreIgnoreCase(String nombre);
}