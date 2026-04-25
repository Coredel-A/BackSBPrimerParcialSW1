package com.workflow.seguridad.repository;

import com.workflow.seguridad.model.usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface usuariorepository extends MongoRepository<usuario , String> {
    Optional<usuario> findByEmail(String email);

    Boolean existsByEmail(String email); 
    // NUEVO: Busca usuarios que tengan cualquiera de los roles en la lista y estén activos
    List<usuario> findByRolInAndActivoTrue(List<String> roles);
    
} 
