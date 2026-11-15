package com.example.Studio21.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee "Authorization": si el token es válido, deja al usuario autenticado
 * en el SecurityContext (principal = id del usuario, autoridad = ROLE_{rol}).
 * Este filtro no decide cuáles rutas son públicas y cuáles no.
 *
 * No lleva @Component para evitar que Spring Boot lo registre
 * como filtro global; solo se corre dentro de /api/.
 */
public class JwtAuthFilter extends OncePerRequestFilter {
    /** Avisa al manejador del error 401 que llegó un token, pero era inválido. */
    public static final String ATRIBUTO_TOKEN_INVALIDO = "studio21.tokenInvalido";
    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(PREFIJO)) {
            try {
                Claims claims = jwtService.validar(header.substring(PREFIJO.length()).trim());
                Long idUsuario = Long.valueOf(claims.getSubject());
                String rol = claims.get(JwtService.CLAIM_ROL, String.class);
                var autenticacion = UsernamePasswordAuthenticationToken.authenticated(
                        idUsuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
                request.setAttribute(ATRIBUTO_TOKEN_INVALIDO, Boolean.TRUE);
            }
        }
        chain.doFilter(request, response);
    }
}