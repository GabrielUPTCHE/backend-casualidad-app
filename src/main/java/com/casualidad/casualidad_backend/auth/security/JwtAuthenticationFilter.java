package com.casualidad.casualidad_backend.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.casualidad.casualidad_backend.auth.repository.TokenRepository;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Si no hay token o no empieza con Bearer, sigue la cadena (y fallará en rutas protegidas)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extraer el token
        jwt = authHeader.substring(7);
        
        try {
            boolean tokenActivo = tokenRepository.findByAccessToken(jwt)
                    .map(token -> Boolean.TRUE.equals(token.getActivo()))
                    .orElse(false);

            if (!tokenActivo) {
                filterChain.doFilter(request, response);
                return;
            }

            userEmail = jwtService.extractUsername(jwt);
            
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                
                // EXTRA: Según tu diagrama ER, podrías consultar la tabla TOKENS aquí
                // boolean isTokenActivoEnBD = tokenRepository.findByAccessToken(jwt).map(t -> t.isActivo()).orElse(false);

                // 3. Validar caducidad y pertenencia
                if (jwtService.isTokenValid(jwt, userDetails) /* && isTokenActivoEnBD */) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Si el token expiró (ExpiredJwtException) o es inválido, caerá aquí.
            // No seteamos el contexto de seguridad, por lo que Spring devolverá un 403 Forbidden o 401 Unauthorized
            System.out.println("Token inválido o expirado: " + e.getMessage());
        }

        // 4. Continuar con la petición
        filterChain.doFilter(request, response);
    }
}