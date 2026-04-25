package com.workflow.politicas.service;

import com.workflow.politicas.dto.departamentorequestdto;
import com.workflow.politicas.dto.departamentoresponsedto;
import com.workflow.politicas.model.departamento;
import com.workflow.politicas.repository.departamentorepository;
import com.workflow.politicas.repository.politicanegociorepository;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class departamentoservice {
    private final departamentorepository repository;
    private final politicanegociorepository politicaRepo;

    public departamentoservice(departamentorepository repository, politicanegociorepository politicaRepo) {
        this.repository = repository;
        this.politicaRepo = politicaRepo;
    }

    public List<departamentoresponsedto> listarTodos() {
        return repository.findAll().stream().map(this::mapToResponse).toList();
    }

    public departamentoresponsedto crear(departamentorequestdto dto) {
        departamento depto = new departamento();
        depto.setNombre(dto.getNombre());
        depto.setDescripcion(dto.getDescripcion());
        depto.setEmpresaId(dto.getEmpresaId());
        depto.setResponsablesIds(dto.getResponsablesIds() != null ? dto.getResponsablesIds() : new java.util.ArrayList<>());
        return mapToResponse(repository.save(depto));
    }

    public departamentoresponsedto actualizar(String id, departamentorequestdto dto) {
        departamento depto = repository.findById(id).orElseThrow(() -> new RuntimeException("No encontrado"));
        depto.setNombre(dto.getNombre());
        depto.setDescripcion(dto.getDescripcion());
        depto.setResponsablesIds(dto.getResponsablesIds());
        return mapToResponse(repository.save(depto));
    }

    public void eliminar(String id) {
        // VALIDACIÓN: ¿Alguna política tiene un nodo asignado a este departamento?
        // En MongoDB, para buscar dentro de una lista de objetos (nodos) usamos el punto
        boolean enUso = politicaRepo.existsByNodosDepartamentoId(id);
        
        if (enUso) {
            throw new RuntimeException("No se puede eliminar: El departamento tiene tareas asignadas en el diseñador.");
        }
        repository.deleteById(id);
    }

    // Métodos para responsables específicos
    public departamentoresponsedto agregarResponsable(String id, String usuarioId) {
        departamento depto = repository.findById(id).orElseThrow();
        if (!depto.getResponsablesIds().contains(usuarioId)) {
            depto.getResponsablesIds().add(usuarioId);
        }
        return mapToResponse(repository.save(depto));
    }

    // Mapper manual rápido
    private departamentoresponsedto mapToResponse(departamento depto) {
        departamentoresponsedto res = new departamentoresponsedto();
        res.setId(depto.getId());
        res.setNombre(depto.getNombre());
        res.setDescripcion(depto.getDescripcion());
        res.setEmpresaId(depto.getEmpresaId());
        res.setResponsablesIds(depto.getResponsablesIds());
        return res;
    }

    public departamentoresponsedto obtenerPorId(String id) {
        departamento depto = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Departamento no encontrado"));
        return mapToResponse(depto);
    }

    public departamentoresponsedto quitarResponsable(String id, String usuarioId) {
        departamento depto = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Departamento no encontrado"));
        
        if (depto.getResponsablesIds() != null) {
            depto.getResponsablesIds().remove(usuarioId);
        }
        
        return mapToResponse(repository.save(depto));
    }
}
