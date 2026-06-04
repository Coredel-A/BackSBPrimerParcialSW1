package com.workflow.politicas.dto;

import lombok.Data;
import java.util.List;

@Data
public class iachatresponsedto {
    private String respuestaTexto;
    private List<iaacciondto> acciones;
    private boolean tieneCambios;
    private String textoTranscrito; // Para CU08 (voz)    
}
