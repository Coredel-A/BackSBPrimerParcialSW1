package com.workflow.seguridad.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Document(collection = "usuarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class usuario {

    @Id
    private String id;

    private String nombre;
    
    private String apellido;

    @Indexed(unique = true)
    private String email;

    @JsonIgnore
    private String passwordHash;

    // Los roles definidos: "ADMINISTRADOR", "COLABORADOR", "FUNCIONARIO", "CLIENTE"
    private String rol;

    // ID del departamento al que pertenece (opcional para CLIENTE)
    private String departamentoId;

    private boolean activo;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
