package com.workflow.seguridad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class usuarioresponsedto {
    private String id;
    private String nombre;
    private String apellido;
    private String email;
    private String rol;
    private String departamentoId;
    private boolean activo;    
}
