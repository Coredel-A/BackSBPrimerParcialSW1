package com.workflow.politicas.model;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.annotation.Id;

@Data
public class nodo {
    @Id
    private String id;
    private String nombre;
    private String descripcion;
    private String departamentoId; // ID del carril al que pertenece físicamente
    private String tipo; // INICIO, FIN, TAREA, DECISION, etc.
    private Map<String, Double> posicion; // Guardará {x, y} relativos al carril
    private String condicion;    
    
    // Campo informativo/documentación semántica
    private Boolean posicionXAbsoluta = false;

    public void setX(double x) {
        if (this.posicion == null) this.posicion = new HashMap<>();
        this.posicion.put("x", x);
    }

    public void setY(double y) {
        if (this.posicion == null) this.posicion = new HashMap<>();
        this.posicion.put("y", y);
    }
}