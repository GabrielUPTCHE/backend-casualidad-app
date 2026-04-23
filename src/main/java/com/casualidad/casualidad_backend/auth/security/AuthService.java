package com.casualidad.casualidad_backend.auth.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.casualidad.casualidad_backend.auth.dto.AuthDTOs.LoginRequest;
import com.casualidad.casualidad_backend.auth.dto.AuthDTOs.LoginResponse;
import com.casualidad.casualidad_backend.auth.dto.AuthDTOs.UsuarioInfo;
import com.casualidad.casualidad_backend.auth.dto.RegisterRequest;
import com.casualidad.casualidad_backend.auth.dto.RegisterResponse;
import com.casualidad.casualidad_backend.auth.entity.Rol;
import com.casualidad.casualidad_backend.auth.entity.Token;
import com.casualidad.casualidad_backend.auth.entity.Usuario;
import com.casualidad.casualidad_backend.auth.repository.RolRepository;
import com.casualidad.casualidad_backend.auth.repository.TokenRepository;
import com.casualidad.casualidad_backend.auth.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final RolRepository rolRepository;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.correo(), request.contraseña())
        );

        // 2. Si llegamos aquí, las credenciales son correctas
        Usuario usuario = usuarioRepository.findByCorreo(request.correo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String jwtToken = jwtService.generateToken(usuario);
        String refreshToken = jwtService.generateRefreshToken(usuario);

        // 3. Invalidar tokens anteriores (opcional, para que solo tenga una sesión activa)
        revokeAllUserTokens(usuario);

        // 4. Guardar el nuevo token en la BD según tu diagrama
        saveUserToken(usuario, jwtToken, refreshToken);

        return new LoginResponse(jwtToken, refreshToken, new UsuarioInfo(
                usuario.getId_usuario().toString(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol().getNombreRol()
        ));
    }

    public RegisterResponse register(RegisterRequest request) {
        System.out.println("hace aqui");
        // 1. Validar que el correo no exista ya
        if (usuarioRepository.findByCorreo(request.getCorreo()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        // 2. Buscar el rol por defecto (ej. "CLIENTE" o "USUARIO"). 
        // Asegúrate de que este rol exista en tu tabla ROLES en la BD.
        Rol rol = rolRepository.findByNombreRol(request.getRol())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado en la base de datos"));

        // 3. Crear la entidad Usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setCorreo(request.getCorreo());
        // AQUÍ ENCRIPTAMOS LA CONTRASEÑA ANTES DE GUARDARLA
        usuario.setContraseña(passwordEncoder.encode(request.getContraseña())); 
        usuario.setRol(rol);
        usuario.setActivo(true); // Activo por defecto

        // 4. Guardar en base de datos
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // 5. Generar tokens automáticamente tras el registro (opcional, pero mejora la UX)
        var jwtToken = jwtService.generateToken(usuarioGuardado);
        var refreshToken = jwtService.generateRefreshToken(usuarioGuardado);

        // 6. Guardar los tokens en la tabla TOKENS
        saveUserToken(usuarioGuardado, jwtToken, refreshToken);

        // 7. Devolver respuesta
        return new RegisterResponse(jwtToken, refreshToken, usuarioGuardado.getNombre());
    }

    private void saveUserToken(Usuario usuario, String jwtToken, String refreshToken) {
        Token token = Token.builder()
                .usuario(usuario)
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .activo(true)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(Usuario usuario) {
        var validTokens = tokenRepository.findAllValidTokensByUser(usuario.getId_usuario());
        if (validTokens.isEmpty()) return;
        validTokens.forEach(t -> t.setActivo(false));
        tokenRepository.saveAll(validTokens);
    }
}