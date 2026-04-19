package com.workflow.seguridad.controller;

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

    @GetMapping("/")
    public ResponseEntity<List<usuario>> getAllUsers() {
        return ResponseEntity.ok(usuarioService.getAllUsuarios());
    }

    @PostMapping("/")
    public ResponseEntity<usuario> createUser(@RequestBody usuario usuario) {
        try {
            return ResponseEntity.ok(usuarioService.createUsuario(usuario));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/rol")
    public ResponseEntity<usuario> updateRol(@PathVariable String id, @RequestParam String rol) {
        try {
            return ResponseEntity.ok(usuarioService.updateRol(id, rol));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        try {
            usuarioService.deleteUsuario(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }    
}
