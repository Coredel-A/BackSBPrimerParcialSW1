package com.workflow.seguridad.controller;

import com.workflow.seguridad.dto.usuariorequestdto;
import com.workflow.seguridad.dto.usuarioresponsedto;
import com.workflow.seguridad.model.usuario;
import com.workflow.seguridad.service.usuarioservice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class usuariocontroller {
    private final usuarioservice usuarioService;

    public usuariocontroller(usuarioservice usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<usuarioresponsedto>> getAll() {
        return ResponseEntity.ok(usuarioService.getAllUsuarios());
    }

    @GetMapping("/por-rol")
    public ResponseEntity<List<usuarioresponsedto>> getPorRoles(@RequestParam List<String> roles) {
        return ResponseEntity.ok(usuarioService.listarPorRoles(roles));
    }

    @PostMapping
    public ResponseEntity<usuarioresponsedto> create(@RequestBody usuariorequestdto dto) {
        return ResponseEntity.ok(usuarioService.createUsuario(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<usuarioresponsedto> update(@PathVariable String id, @RequestBody usuariorequestdto dto) {
        return ResponseEntity.ok(usuarioService.updateUsuario(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        // Aquí podrías implementar un borrado lógico (activo = false)
        usuarioService.deleteUsuario(id); 
        return ResponseEntity.noContent().build();
    } 
}
