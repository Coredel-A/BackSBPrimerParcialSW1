package com.workflow.politicas.repository;

import com.workflow.politicas.model.departamento;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface departamentorepository extends MongoRepository<departamento, String> {
    List<departamento> findByEmpresaId(String empresaId);
}
