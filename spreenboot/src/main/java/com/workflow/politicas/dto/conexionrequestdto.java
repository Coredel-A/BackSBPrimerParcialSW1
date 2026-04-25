package com.workflow.politicas.dto;

import lombok.Data;

@Data
public class conexionrequestdto {
    private String nodoOrigenId;
    private String nodoDestinoId;
    private String tipoFlujo; // SECUENCIAL, ALTERNATIVO_SI, etc.
    private String etiqueta;
    private String condicion;    
}
