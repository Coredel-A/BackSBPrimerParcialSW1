package com.workflow.politicas.dto;

import com.workflow.politicas.model.nodo;
import com.workflow.politicas.model.conexion;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class politicaresponsedto {
    private String id;
    private String nombre;
    private String descripcion;
    private String empresaId;
    private String estado;
    private String creadoPorId;
    private List<nodo> nodos;
    private List<conexion> conexiones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;    
}
