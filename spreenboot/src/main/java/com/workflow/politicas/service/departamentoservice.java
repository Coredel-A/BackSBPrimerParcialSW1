package com.workflow.politicas.service;

import com.workflow.politicas.model.departamento;
import com.workflow.politicas.repository.departamentorepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class departamentoservice {
    private final departamentorepository repository;

    public departamentoservice(departamentorepository repository) {
        this.repository = repository;
    }

    public List<departamento> listarTodos() {
        return repository.findAll();
    }

    public departamento crear(departamento depto) {
        return repository.save(depto);
    }

    public departamento buscarPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Departamento no encontrado"));
    }

    public void eliminar(String id) {
        repository.deleteById(id);
    }    
}
