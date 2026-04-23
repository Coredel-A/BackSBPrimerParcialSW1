package com.workflow.politicas.controller;

import com.workflow.politicas.model.politicas;
import com.workflow.politicas.model.nodo;
import com.workflow.politicas.service.politicaservice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
