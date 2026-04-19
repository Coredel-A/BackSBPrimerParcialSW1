package com.workflow.seguridad.service;

import com.workflow.seguridad.model.usuario;
import com.workflow.seguridad.repository.usuariorepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class usuarioservice {
   
    private final usuariorepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public usuarioservice(usuariorepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public usuario createUsuario(usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado.");
        }
        // Encriptar la contraseña antes de guardarla en MongoDB
        usuario.setPasswordHash(passwordEncoder.encode(usuario.getPasswordHash()));
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    public usuario updateRol(String id, String nuevoRol) {
        usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setRol(nuevoRol);
        return usuarioRepository.save(usuario);
    }

    public void deleteUsuario(String id) {
        // En lugar de borrarlo físicamente, hacemos un borrado lógico (recomendado)
        usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }
}
