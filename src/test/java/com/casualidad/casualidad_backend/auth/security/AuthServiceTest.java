package com.casualidad.casualidad_backend.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.casualidad.casualidad_backend.auth.dto.AuthDTOs.LoginRequest;
import com.casualidad.casualidad_backend.auth.dto.AuthDTOs.LoginResponse;
import com.casualidad.casualidad_backend.auth.dto.RegisterRequest;
import com.casualidad.casualidad_backend.auth.dto.RegisterResponse;
import com.casualidad.casualidad_backend.auth.entity.Rol;
import com.casualidad.casualidad_backend.auth.entity.Token;
import com.casualidad.casualidad_backend.auth.entity.Usuario;
import com.casualidad.casualidad_backend.auth.repository.RolRepository;
import com.casualidad.casualidad_backend.auth.repository.TokenRepository;
import com.casualidad.casualidad_backend.auth.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TokenRepository tokenRepository;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RolRepository rolRepository;

    @InjectMocks private AuthService authService;

    @Test
    void loginDebeGenerarTokensYRevocarLosPrevios() {
        LoginRequest request = new LoginRequest("usuario@casualidad.com", "secreta");
        Usuario usuario = usuario(7L, "usuario@casualidad.com", "USUARIO");
        Token tokenActual = Token.builder().id_token(1L).usuario(usuario).accessToken("old-access").refreshToken("old-refresh").activo(true).build();

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(usuarioRepository.findByCorreo("usuario@casualidad.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(usuario)).thenReturn("access-new");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("refresh-new");
        when(tokenRepository.findAllValidTokensByUser(7L)).thenReturn(List.of(tokenActual));

        LoginResponse response = authService.login(request);

        assertEquals("access-new", response.accessToken());
        assertEquals("refresh-new", response.refreshToken());
        assertEquals("USUARIO", response.usuario().rol());
        verify(tokenRepository).saveAll(anyList());
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    void registerDebePersistirUsuarioYRetornarTokens() {
        RegisterRequest request = new RegisterRequest();
        request.setNombre("Nuevo Usuario");
        request.setCorreo("nuevo@casualidad.com");
        request.setContraseña("123456");
        request.setRol("CLIENTE");

        Rol rol = new Rol(3L, "CLIENTE", "BASICO", true);
        Usuario usuarioGuardado = usuario(12L, request.getCorreo(), "CLIENTE");
        usuarioGuardado.setNombre(request.getNombre());

        when(usuarioRepository.findByCorreo(request.getCorreo())).thenReturn(Optional.empty());
        when(rolRepository.findByNombreRol("CLIENTE")).thenReturn(Optional.of(rol));
        when(passwordEncoder.encode("123456")).thenReturn("hashed-password");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);
        when(jwtService.generateToken(usuarioGuardado)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(usuarioGuardado)).thenReturn("refresh-token");

        RegisterResponse response = authService.register(request);

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("Nuevo Usuario", response.nombreUsuario());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("hashed-password", captor.getValue().getContraseña());
    }

    @Test
    void registerDebeFallarSiElCorreoYaExiste() {
        RegisterRequest request = new RegisterRequest();
        request.setCorreo("duplicado@casualidad.com");

        when(usuarioRepository.findByCorreo(request.getCorreo())).thenReturn(Optional.of(new Usuario()));

        assertThrows(RuntimeException.class, () -> authService.register(request));
    }

    private Usuario usuario(Long id, String correo, String rol) {
        Usuario usuario = new Usuario();
        usuario.setId_usuario(id);
        usuario.setCorreo(correo);
        usuario.setNombre("Usuario Test");
        usuario.setActivo(true);
        usuario.setRol(new Rol(1L, rol, "ALTO", true));
        return usuario;
    }
}