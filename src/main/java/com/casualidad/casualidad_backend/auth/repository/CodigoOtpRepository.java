package com.casualidad.casualidad_backend.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.casualidad.casualidad_backend.auth.entity.CodigoOtp;
import com.casualidad.casualidad_backend.auth.entity.Usuario;
import com.casualidad.casualidad_backend.common.domain.enums.PropositoOtp;

import java.util.Optional;

public interface CodigoOtpRepository extends JpaRepository<CodigoOtp, Long> {
    Optional<CodigoOtp> findByUsuarioAndProposito(Usuario usuario, PropositoOtp proposito);
}
