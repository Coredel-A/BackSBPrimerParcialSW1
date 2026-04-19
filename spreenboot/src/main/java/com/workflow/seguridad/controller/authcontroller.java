package com.workflow.seguridad.controller;
import com.workflow.seguridad.service.authservice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class authcontroller {
    private final authservice authService;

    public authcontroller(authservice authService) {
        this.authService = authService;
    }

    // DTOs internos (idealmente muévelos a la carpeta dto)
    public record LoginRequest(String email, String password) {}
    public record AuthResponse(String token) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String token = authService.login(request.email(), request.password());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // En JWT el logout suele ser manejado por el frontend (Angular elimina el token de localStorage).
        // A nivel backend, solo retornamos éxito, a menos que implementes una "blacklist" de tokens.
        return ResponseEntity.ok("Sesión cerrada correctamente (token invalidado en cliente)");
    } 
}
