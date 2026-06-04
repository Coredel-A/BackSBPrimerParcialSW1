package com.workflow.politicas.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class chatinputdto {
    private String mensaje;
    private List<Map<String, String>> historial; 
}
