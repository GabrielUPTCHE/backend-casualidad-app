package com.casualidad.casualidad_backend.auth.security;

import com.casualidad.casualidad_backend.auth.dto.AuthDTOs;
import com.casualidad.casualidad_backend.auth.entity.Rol;
import com.casualidad.casualidad_backend.auth.entity.Usuario;
import com.casualidad.casualidad_backend.auth.repository.RolRepository;
import com.casualidad.casualidad_backend.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Transactional
public class AuthServiceIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    @Test
    void loginExitosoConCredencialesValidas() {
        // Preparar rol en BD
        Rol rol = new Rol();
        rol.setNombreRol("CONTADOR");
        rol.setNivelPermisos("ALL");
        rol.setActivo(true);
        rol = rolRepository.save(rol);

        // Preparar usuario activo en BD con contraseña encriptada
        Usuario usuario = new Usuario();
        usuario.setNombre("Contador");
        usuario.setCorreo("contador@yopmail.com");
        usuario.setContraseña(passwordEncoder.encode("Test@1234"));
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario = usuarioRepository.save(usuario);

        // Mockear AuthenticationManager para que la autenticación sea exitosa
        Mockito.when(authenticationManager.authenticate(Mockito.any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(usuario.getCorreo(), "Test@1234"));

        // Mockear JwtService para devolver tokens conocidos
        Mockito.when(jwtService.generateToken(Mockito.any())).thenReturn("jwt-token-sample");
        Mockito.when(jwtService.generateRefreshToken(Mockito.any())).thenReturn("refresh-token-sample");

        // Ejecutar login
        AuthDTOs.LoginRequest request = new AuthDTOs.LoginRequest("contador@yopmail.com", "Test@1234");
        var response = authService.login(request);

        // Verificaciones
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isNotEmpty();
        assertThat(response.usuario().rol()).isEqualTo(rol.getNombreRol());
    }
}
