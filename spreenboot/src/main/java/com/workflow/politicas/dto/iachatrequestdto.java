package com.workflow.politicas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class iachatrequestdto {
    private String mensaje;
    private Map<String, Object> contextoDiagrama;
    private String politicaId;
    private List<Map<String, String>> historial; 
}
