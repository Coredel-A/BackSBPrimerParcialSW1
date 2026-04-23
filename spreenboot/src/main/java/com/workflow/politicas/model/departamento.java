package com.workflow.politicas.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document(collection = "departamentos")
public class departamento {
    @Id
    private String id;
    private String nombre;
    private String descripcion;
    private String empresaId;
    private List<String> responsablesIds; // IDs de los Usuarios con rol ADMINISTRADOR/FUNCIONARIO    
}
