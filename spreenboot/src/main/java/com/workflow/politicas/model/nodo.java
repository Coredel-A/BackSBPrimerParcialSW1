package com.workflow.politicas.model;

import lombok.Data;
import java.util.Map;

@Data
public class nodo {
    private String id;
    private String nombre;
    private String descripcion;
    private String departamentoId;
    private String tipo; // INICIO, FIN, TAREA, DECISION, etc.
    private Map<String, Double> posicion; // Para guardar x, y de la interfaz visual
    private String condicion;    
}
