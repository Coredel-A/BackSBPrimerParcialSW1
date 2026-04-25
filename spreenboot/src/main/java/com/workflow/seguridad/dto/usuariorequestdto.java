package com.workflow.seguridad.dto;

import lombok.Data;

@Data
public class usuariorequestdto {
    private String nombre;
    private String apellido;
    private String email;
    private String password; // Aquí el front envía la clave plana antes de hashear
    private String rol;
    private String departamentoId;    
}
