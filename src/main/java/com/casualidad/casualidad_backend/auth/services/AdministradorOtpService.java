package com.casualidad.casualidad_backend.auth.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.casualidad.casualidad_backend.auth.entity.CodigoOtp;
import com.casualidad.casualidad_backend.auth.entity.Usuario;
import com.casualidad.casualidad_backend.auth.repository.CodigoOtpRepository;
import com.casualidad.casualidad_backend.common.domain.enums.PropositoOtp;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdministradorOtpService {

    private final CodigoOtpRepository otpRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public String generarYGuardarOtp(Usuario usuario, PropositoOtp proposito, String valorFuturo) {
        CodigoOtp otp = otpRepository.findByUsuarioAndProposito(usuario, proposito)
                .orElse(new CodigoOtp());

        if (otp.estaBloqueado()) {
            throw new IllegalStateException("Demasiados intentos fallidos. Intenta de nuevo en 15 minutos.");
        }

        otp.setUsuario(usuario);
        otp.setCodigo(String.format("%06d", secureRandom.nextInt(999999)));
        otp.setProposito(proposito);
        otp.setIntentosFallidos(0);
        otp.setFechaExpiracion(LocalDateTime.now().plusMinutes(10));
        otp.setBloqueadoHasta(null);
        otp.setValorFuturo(valorFuturo); 

        otpRepository.save(otp);
        return otp.getCodigo();
    }

    @Transactional
    public CodigoOtp validarOtp(Usuario usuario, String codigoDigitado, PropositoOtp proposito) {
        CodigoOtp otp = otpRepository.findByUsuarioAndProposito(usuario, proposito)
                .orElseThrow(() -> new IllegalArgumentException("No hay un código activo para esta acción."));

        if (otp.estaBloqueado()) {
            throw new IllegalStateException("Demasiados intentos fallidos. Intenta de nuevo en 15 minutos.");
        }

        if (otp.estaExpirado()) {
            throw new IllegalArgumentException("El código ha expirado. Solicita uno nuevo.");
        }

        if (!otp.getCodigo().equals(codigoDigitado)) {
            otp.setIntentosFallidos(otp.getIntentosFallidos() + 1);
            int intentosRestantes = 5 - otp.getIntentosFallidos();

            if (intentosRestantes <= 0) {
                otp.setBloqueadoHasta(LocalDateTime.now().plusMinutes(15));
                otpRepository.save(otp);
                throw new IllegalStateException("Has agotado tus intentos. Cuenta bloqueada temporalmente para esta acción.");
            }
            otpRepository.save(otp);
            throw new IllegalArgumentException("Código incorrecto. Te quedan " + intentosRestantes + " intentos.");
        }

        return otp; 
    }

    @Transactional
    public void eliminarOtpUsado(CodigoOtp otp) {
        otpRepository.delete(otp); 
    }
}
