package com.workflow.politicas.dto;

import lombok.Data;
import java.util.Map;

@Data
public class nodorequestdto {
    private String nombre;
    private String descripcion;
    private String departamentoId;
    private String tipo; // INICIO, FIN, TAREA, DECISION, etc.
    private Map<String, Double> posicion; 
    private String condicion;    
}
