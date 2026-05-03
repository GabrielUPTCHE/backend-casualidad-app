package com.casualidad.casualidad_backend.auth.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.casualidad.casualidad_backend.auth.entity.CodigoOtp;
import com.casualidad.casualidad_backend.auth.entity.Usuario;
import com.casualidad.casualidad_backend.auth.repository.UsuarioRepository;
import com.casualidad.casualidad_backend.common.domain.enums.PropositoOtp;

@Service
@RequiredArgsConstructor
public class SeguridadPasswordService {

    private final AdministradorOtpService otpService;
    private final EmailService emailService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public void solicitarCodigo(Usuario usuario) {
        String codigo = otpService.generarYGuardarOtp(usuario, PropositoOtp.CAMBIO_PASSWORD, null);
        emailService.enviarCorreo(usuario.getCorreo(), "Código de Seguridad", "Tu código es: " + codigo);
    }

    @Transactional
    public void cambiarPassword(Usuario usuario, String codigo, String nuevaPassword) {
        CodigoOtp otpValido = otpService.validarOtp(usuario, codigo, PropositoOtp.CAMBIO_PASSWORD);
        
        usuario.setContraseña(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
        otpService.eliminarOtpUsado(otpValido);
    }
}