package com.workflow.politicas.dto;

import lombok.Data;
import java.util.Map;

@Data
public class iaacciondto {
    private String tipo; // AGREGAR_NODO, ELIMINAR_NODO, etc.
    private Map<String, Object> datos;    
}
