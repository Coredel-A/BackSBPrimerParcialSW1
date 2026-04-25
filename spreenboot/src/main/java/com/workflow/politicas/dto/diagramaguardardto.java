package com.workflow.politicas.dto;

import com.workflow.politicas.model.nodo;
import com.workflow.politicas.model.conexion;
import lombok.Data;
import java.util.List;

@Data
public class diagramaguardardto {
    private List<nodo> nodos;
    private List<conexion> conexiones;  
}
