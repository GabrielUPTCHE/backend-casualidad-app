package com.casualidad.casualidad_backend.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.casualidad.casualidad_backend.auth.entity.Token;

import java.util.List;
import java.util.Optional;
 
@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
 
    Optional<Token> findByAccessToken(String accessToken);

    @Query("SELECT t FROM Token t WHERE t.usuario.id_usuario = :id AND t.activo = true")
    List<Token> findAllValidTokensByUser(Long id);
}