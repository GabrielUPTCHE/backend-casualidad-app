package com.casualidad.casualidad_backend.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TOKENS")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_token;

    @Column(length = 250, name = "access_token")
    private String accessToken;

    @Column(length = 250, name = "refresh_token")
    private String refreshToken;

    private Boolean activo;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}