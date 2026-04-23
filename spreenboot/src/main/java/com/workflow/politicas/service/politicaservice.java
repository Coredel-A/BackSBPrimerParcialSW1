package com.workflow.politicas.service;

import com.workflow.politicas.model.politicas;
import com.workflow.politicas.model.nodo;
import com.workflow.politicas.model.conexion;
import com.workflow.politicas.repository.politicanegociorepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class politicaservice {
    private final politicanegociorepository repository;

    public politicaservice(politicanegociorepository repository) {
        this.repository = repository;
    }

    public List<politicas> listarTodas() {
        return repository.findAll();
    }

    public politicas crear(politicas politica) {
        politica.setCreatedAt(LocalDateTime.now());
        politica.setUpdatedAt(LocalDateTime.now());
        // Inicializamos listas si vienen nulas
        if (politica.getNodos() == null) politica.setNodos(new ArrayList<>());
        if (politica.getConexiones() == null) politica.setConexiones(new ArrayList<>());
        if (politica.getEstado() == null) politica.setEstado("BORRADOR");
        
        return repository.save(politica);
    }

    public politicas buscarPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Política no encontrada"));
    }

    // Lógica para actualizar nodos (muy útil para el editor visual que haremos después)
    public politicas actualizarNodos(String id, List<nodo> nuevosNodos) {
        politicas politica = buscarPorId(id);
        politica.setNodos(nuevosNodos);
        politica.setUpdatedAt(LocalDateTime.now());
        return repository.save(politica);
    }

    public void eliminar(String id) {
        repository.deleteById(id);
    }    
}
