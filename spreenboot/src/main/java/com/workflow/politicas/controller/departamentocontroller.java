package com.workflow.politicas.controller;

import com.workflow.politicas.dto.departamentorequestdto;
import com.workflow.politicas.dto.departamentoresponsedto;
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
    public List<departamentoresponsedto> getAll() {
        return service.listarTodos();
    }

    @PostMapping
    public ResponseEntity<departamentoresponsedto> create(@RequestBody departamentorequestdto dto) {
        return ResponseEntity.ok(service.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<departamentoresponsedto> update(@PathVariable String id, @RequestBody departamentorequestdto dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
    
    // Endpoints de responsables
    @PostMapping("/{id}/responsables/{usuarioId}")
    public ResponseEntity<departamentoresponsedto> addResponsable(@PathVariable String id, @PathVariable String usuarioId) {
        return ResponseEntity.ok(service.agregarResponsable(id, usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<departamentoresponsedto> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    // DELETE /api/departamentos/{id}/responsables/{usuarioId}
    @DeleteMapping("/{id}/responsables/{usuarioId}")
    public ResponseEntity<departamentoresponsedto> removeResponsable(
            @PathVariable String id, 
            @PathVariable String usuarioId) {
        return ResponseEntity.ok(service.quitarResponsable(id, usuarioId));
    }
}
