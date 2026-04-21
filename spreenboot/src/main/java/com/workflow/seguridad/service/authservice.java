package com.workflow.seguridad.service;

import com.workflow.seguridad.model.usuario;
import com.workflow.seguridad.repository.usuariorepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class authservice implements UserDetailsService {
 
    private final usuariorepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    // 1. INYECTA jwtutil (No crees llaves aquí)
    private final com.workflow.seguridad.config.jwtutil jwtUtil; 

    // 2. Agrégalo al constructor
    public authservice(usuariorepository usuarioRepository, PasswordEncoder passwordEncoder, com.workflow.seguridad.config.jwtutil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        usuario user = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // Retorna un objeto User de Spring Security con los datos de tu MongoDB
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRol()) // Asegúrate de que el rol sea un String
                .build();
    }
    public String login(String email, String password) {
        usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));

        if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        if (!usuario.isActivo()) {
            throw new RuntimeException("El usuario está inactivo");
        }

        return jwtUtil.generarToken(usuario);
    }
}
