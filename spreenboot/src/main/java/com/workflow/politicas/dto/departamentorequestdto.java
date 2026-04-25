package com.workflow.politicas.dto;

import lombok.Data;
import java.util.List;

@Data
public class departamentorequestdto {
    private String nombre;
    private String descripcion;
    private String empresaId;
    private List<String> responsablesIds;
}
