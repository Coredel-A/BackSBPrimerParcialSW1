package com.workflow.politicas.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.workflow.politicas.dto.chatinputdto;
import com.workflow.politicas.dto.iachatrequestdto;
import com.workflow.politicas.dto.iachatresponsedto;
import com.workflow.politicas.service.politicaservice;
import com.workflow.shared.util.iaclientservice;

@RestController
@RequestMapping("/api/politicas/{id}/chat")
public class chatiacontroller {
    private final iaclientservice iaClient;
    private final politicaservice politicaService;

    public chatiacontroller(iaclientservice iaClient, politicaservice politicaService) {
        this.iaClient = iaClient;
        this.politicaService = politicaService;
    }

    @PostMapping
    public ResponseEntity<?> chatTexto(@PathVariable String id, @RequestBody chatinputdto input) {
        try {
            System.out.println("Iniciando chat para política: " + id);
            
            Map<String, Object> contexto = politicaService.obtenerDiagramaParaIA(id);
            System.out.println("================================");
            System.out.println(contexto);
            System.out.println("================================");

            iachatrequestdto request = iachatrequestdto.builder()
                    .mensaje(input.getMensaje())
                    .contextoDiagrama(contexto)
                    .politicaId(id)
                    .historial(input.getHistorial())
                    .build();

            iachatresponsedto response = iaClient.enviarChatTexto(request);
            System.out.println("Respuesta de IA: " + response);

            if (response.isTieneCambios()) {
                politicaService.aplicarAccionesIA(id, response.getAcciones());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // Esto imprimirá el error real en tu consola de IntelliJ/VSCode
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
