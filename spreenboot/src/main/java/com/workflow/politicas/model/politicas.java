package com.workflow.politicas.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "politicas_negocio")
public class politicas {
    @Id
    private String id;
    private String nombre;
    private String descripcion;
    private String empresaId;
    private String creadoPorId;
    private String estado; // BORRADOR, ACTIVA, INACTIVA
    
    private List<String> colaboradoresIds;
    private List<nodo> nodos;
    private List<conexion> conexiones;

    // NUEVOS CAMPOS ENFOQUE SWIMLANES (CARRILES)
    private List<String> ordenCarriles = new ArrayList<>(); // Almacena IDs de departamentos en orden vertical
    private int alturaCarril = 150; // Altura por defecto de cada carril en píxeles

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();    
}