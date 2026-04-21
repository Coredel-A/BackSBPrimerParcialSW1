package com.workflow.seguridad.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class jwtfilter extends OncePerRequestFilter {

    private final jwtutil jwtUtil;

    public jwtfilter(jwtutil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (jwtUtil.validarToken(token)) {
                String email = jwtUtil.obtenerEmailDeToken(token);
                String rol = jwtUtil.obtenerRolDeToken(token);

                // IMPORTANTE: Aquí creamos la identidad del usuario para Spring
                // Usamos el rol que viene en el token
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email, 
                        null, 
                        List.of(new SimpleGrantedAuthority(rol)) // O "ROLE_" + rol si usas hasRole
                );
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // ESTA LÍNEA ES LA QUE QUITA EL 403: Registra al usuario en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                System.out.println(">>> ¡Usuario " + email + " con rol " + rol + " autorizado!");
            }
        }

        filterChain.doFilter(request, response);
    }
}