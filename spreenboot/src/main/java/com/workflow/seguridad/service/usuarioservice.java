package com.workflow.seguridad.service;

import com.workflow.seguridad.dto.usuariorequestdto;
import com.workflow.seguridad.dto.usuarioresponsedto;
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

    public List<usuarioresponsedto> getAllUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<usuarioresponsedto> listarPorRoles(List<String> roles) {
        return usuarioRepository.findByRolInAndActivoTrue(roles).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public usuarioresponsedto createUsuario(usuariorequestdto dto) {
        usuario user = mapToEntity(dto);
        // Hasheamos la contraseña antes de guardar
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        return mapToResponse(usuarioRepository.save(user));
    }

    public usuarioresponsedto updateUsuario(String id, usuariorequestdto dto) {
        usuario user = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        user.setNombre(dto.getNombre());
        user.setApellido(dto.getApellido());
        user.setRol(dto.getRol());
        user.setDepartamentoId(dto.getDepartamentoId());
        
        // Solo actualizamos contraseña si envían una nueva
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        
        return mapToResponse(usuarioRepository.save(user));
    }

    public void deleteUsuario(String id) {
        // En lugar de borrarlo físicamente, hacemos un borrado lógico (recomendado)
        usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    // Convierte de Entidad a DTO de Respuesta
    private usuarioresponsedto mapToResponse(usuario user) {
        return usuarioresponsedto.builder()
                .id(user.getId())
                .nombre(user.getNombre())
                .apellido(user.getApellido())
                .email(user.getEmail())
                .rol(user.getRol())
                .departamentoId(user.getDepartamentoId())
                .activo(user.isActivo())
                .build();
    }

    // Convierte de DTO de Request a Entidad
    private usuario mapToEntity(usuariorequestdto dto) {
        return usuario.builder()
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .email(dto.getEmail())
                .rol(dto.getRol())
                .departamentoId(dto.getDepartamentoId())
                .activo(true) // Por defecto activos al crear
                .build();
    }
}
