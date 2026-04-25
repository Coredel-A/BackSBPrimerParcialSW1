package com.workflow.politicas.repository;

import com.workflow.politicas.model.politicas;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface politicanegociorepository extends MongoRepository<politicas, String> {
    List<politicas> findByEmpresaId(String empresaId);
    List<politicas> findByEstado(String estado);
    List<politicas> findByColaboradoresIdsContaining(String usuarioId);
    // Esto busca si en la lista "nodos" de la política, algún "departamentoId" coincide
    boolean existsByNodosDepartamentoId(String departamentoId);
}
