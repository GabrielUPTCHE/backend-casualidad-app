package com.casualidad.casualidad_backend.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.casualidad.casualidad_backend.auth.entity.Usuario;

import java.util.Optional;
 
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
 
    Optional<Usuario> findByCorreo(String correo);
}
 