package com.casualidad.casualidad_backend.auth.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.casualidad.casualidad_backend.auth.entity.CodigoOtp;
import com.casualidad.casualidad_backend.auth.entity.Usuario;
import com.casualidad.casualidad_backend.auth.repository.UsuarioRepository;
import com.casualidad.casualidad_backend.common.domain.enums.PropositoOtp;

@Service
@RequiredArgsConstructor
public class SeguridadCorreoService {

    private final AdministradorOtpService otpService;
    private final EmailService emailService;
    private final UsuarioRepository usuarioRepository;

    public void solicitarCodigoCorreoActual(Usuario usuario) {
        String codigo = otpService.generarYGuardarOtp(usuario, PropositoOtp.CAMBIO_CORREO_ACTUAL, null);
        emailService.enviarCorreo(usuario.getCorreo(), "Validación de Identidad", "Tu código es: " + codigo);
    }

    @Transactional
    public void validarActualYSolicitarNuevo(Usuario usuario, String codigoActual, String nuevoCorreo) {
        CodigoOtp otpActual = otpService.validarOtp(usuario, codigoActual, PropositoOtp.CAMBIO_CORREO_ACTUAL);
        otpService.eliminarOtpUsado(otpActual);

        String codigoNuevo = otpService.generarYGuardarOtp(usuario, PropositoOtp.CAMBIO_CORREO_NUEVO, nuevoCorreo);
        emailService.enviarCorreo(nuevoCorreo, "Confirma tu nuevo correo", "Tu código es: " + codigoNuevo);
    }

    @Transactional
    public void confirmarCambioDeCorreo(Usuario usuario, String codigoNuevo) {
        CodigoOtp otpValido = otpService.validarOtp(usuario, codigoNuevo, PropositoOtp.CAMBIO_CORREO_NUEVO);
        String correoConfirmado = otpValido.getValorFuturo();

        usuario.setCorreo(correoConfirmado);
        usuarioRepository.save(usuario);

        otpService.eliminarOtpUsado(otpValido);
    }
}
