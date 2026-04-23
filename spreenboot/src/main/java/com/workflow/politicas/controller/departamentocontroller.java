package com.workflow.politicas.controller;

import com.workflow.politicas.model.departamento;
import com.workflow.politicas.service.departamentoservice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departamentos")
public class departamentocontroller {
    private final departamentoservice service;

    public departamentocontroller(departamentoservice service) {
        this.service = service;
    }

    @GetMapping
    public List<departamento> getAll() {
        return service.listarTodos();
    }

    @PostMapping
    public ResponseEntity<departamento> create(@RequestBody departamento depto) {
        return ResponseEntity.ok(service.crear(depto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.eliminar(id);
        return ResponseEntity.ok().build();
    }    
}
