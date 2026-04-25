package com.workflow.politicas.controller;

import com.workflow.politicas.model.politicas;
import com.workflow.politicas.model.nodo;
import com.workflow.politicas.service.politicaservice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.workflow.politicas.dto.*;
import com.workflow.politicas.model.nodo;
import com.workflow.politicas.model.conexion;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/politicas")
public class politicacontroller {
    private final politicaservice service;

    public politicacontroller(politicaservice service) {
        this.service = service;
    }

    @GetMapping
    public List<politicas> getAll() {
        return service.listarTodas();
    }

    @PostMapping
    public ResponseEntity<politicas> create(@RequestBody politicas politica) {
        return ResponseEntity.ok(service.crear(politica));
    }

    @GetMapping("/{id}")
    public ResponseEntity<politicas> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}/nodos")
    public ResponseEntity<politicas> updateNodos(@PathVariable String id, @RequestBody List<nodo> nodos) {
        return ResponseEntity.ok(service.actualizarNodos(id, nodos));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.eliminar(id);
        return ResponseEntity.ok().build();
    }    

    // OBTENER DIAGRAMA COMPLETO
    @GetMapping("/{id}/diagrama")
    public ResponseEntity<politicas> obtenerDiagrama(@PathVariable String id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    // AGREGAR NODO
    @PostMapping("/{id}/nodos")
    public ResponseEntity<politicas> agregarNodo(@PathVariable String id, @RequestBody nodo nodo) {
        return ResponseEntity.ok(service.agregarNodo(id, nodo));
    }

    // EDITAR NODO
    @PutMapping("/{id}/nodos/{nodoId}")
    public ResponseEntity<politicas> actualizarNodo(
            @PathVariable String id, 
            @PathVariable String nodoId, 
            @RequestBody nodo nodo) {
        return ResponseEntity.ok(service.actualizarNodo(id, nodoId, nodo));
    }

    // ELIMINAR NODO (Limpia conexiones automáticamente en el service)
    @DeleteMapping("/{id}/nodos/{nodoId}")
    public ResponseEntity<politicas> eliminarNodo(@PathVariable String id, @PathVariable String nodoId) {
        return ResponseEntity.ok(service.eliminarNodo(id, nodoId));
    }

    // MOVER NODO (Endpoint ligero para PATCH)
    @PatchMapping("/{id}/nodos/{nodoId}/posicion")
    public ResponseEntity<politicas> moverNodo(
            @PathVariable String id, 
            @PathVariable String nodoId, 
            @RequestBody posicionupdatedto pos) {
        return ResponseEntity.ok(service.moverNodo(id, nodoId, pos));
    }

    @GetMapping("/{id}/validar")
    public ResponseEntity<Map<String, Object>> validarPolitica(@PathVariable String id) {
        try {
            service.validarDiagramaCompleto(id);
            return ResponseEntity.ok(Map.of("valido", true, "mensaje", "El diagrama es consistente."));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Map.of("valido", false, "mensaje", e.getMessage()));
        }
    }
    // AGREGAR CONEXIÓN (La que causaba el 404)
    @PostMapping("/{id}/conexiones")
    public ResponseEntity<politicas> agregarConexion(
            @PathVariable String id, 
            @RequestBody conexion nuevaConexion) {
        return ResponseEntity.ok(service.agregarConexion(id, nuevaConexion));
    }

    // ELIMINAR CONEXIÓN
    @DeleteMapping("/{id}/conexiones/{conexionId}")
    public ResponseEntity<Void> eliminarConexion(
            @PathVariable String id, 
            @PathVariable String conexionId) {
        service.eliminarConexion(id, conexionId);
        return ResponseEntity.ok().build();
    }

    // GUARDAR TODO EL DIAGRAMA (Nodos y Conexiones de golpe)
    @PutMapping("/{id}/diagrama")
    public ResponseEntity<politicas> guardarDiagramaCompleto(
        @PathVariable String id, 
        @RequestBody diagramaguardardto diagrama) { // <--- Usa el nombre de tu clase aquí
    return ResponseEntity.ok(service.actualizarDiagramaCompleto(
        id, 
        diagrama.getNodos(), 
        diagrama.getConexiones()
    ));
}
}
