package com.workflow.politicas.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.Map;

@Controller
public class diagramawebsocketcontroller {

    private final SimpMessagingTemplate messagingTemplate;

    public diagramawebsocketcontroller(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Recibe la nueva posición de un nodo.
     * El cliente envía a: /app/diagrama/{id}/nodo-movido
     * El servidor rebota a: /topic/diagrama/{id}
     */
    @MessageMapping("/diagrama/{politicaId}/nodo-movido")
    public void broadcastMovimiento(
            @DestinationVariable String politicaId, 
            Map<String, Object> payload) {
        
        // El payload contiene { nodoId, x, y }
        // Lo enviamos a todos los que estén escuchando esa política
        messagingTemplate.convertAndSend("/topic/diagrama/" + politicaId, (Object) payload);
    }

    /**
     * Informa sobre cambios estructurales (nodos agregados, eliminados, conexiones)
     */
    @MessageMapping("/diagrama/{politicaId}/cambio")
    public void broadcastCambioGeneral(
            @DestinationVariable String politicaId, 
            Map<String, Object> payload) {
        
        // payload: { tipo: 'NODO_ELIMINADO', id: '...' }
        messagingTemplate.convertAndSend("/topic/diagrama/" + politicaId, (Object) payload);
    }
}
