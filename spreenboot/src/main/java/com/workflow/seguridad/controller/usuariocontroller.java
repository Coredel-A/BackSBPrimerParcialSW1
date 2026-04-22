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

    @GetMapping
    public ResponseEntity<List<usuario>> getAllUsers() {
        return ResponseEntity.ok(usuarioService.getAllUsuarios());
    }

    @PostMapping
    public ResponseEntity<usuario> createUser(@RequestBody usuario usuario) {
        try {
            return ResponseEntity.ok(usuarioService.createUsuario(usuario));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<usuario> updateUsuario(@PathVariable String id, @RequestBody usuario usuarioDatos) {
        try {
            // En tu usuarioService, crea un método que actualice todos los campos
            return ResponseEntity.ok(usuarioService.updateUsuariocompleto(id, usuarioDatos));
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
