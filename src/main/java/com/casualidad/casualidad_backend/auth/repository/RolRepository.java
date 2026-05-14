package com.casualidad.casualidad_backend.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.casualidad.casualidad_backend.auth.entity.Rol;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombreRol(String nombreRol); 
}
