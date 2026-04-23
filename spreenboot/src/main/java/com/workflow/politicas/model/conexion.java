package com.workflow.politicas.model;
import lombok.Data;

@Data
public class conexion {
    private String id;
    private String nodoOrigenId;
    private String nodoDestinoId;
    private String tipoFlujo; // SECUENCIAL, ALTERNATIVO_SI, etc.
    private String etiqueta;
    private String condicion;    
}
