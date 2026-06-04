package com.workflow.shared.util;

import com.workflow.politicas.dto.iachatrequestdto;
import com.workflow.politicas.dto.iachatresponsedto;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class iaclientservice {
    private final WebClient webClient;
    private final ObjectMapper objectMapper; // Para convertir mapas a JSON string en multipart

    public iaclientservice(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8000").build();
        this.objectMapper = objectMapper;
    }

    // CU07: Enviar texto a FastAPI
    public iachatresponsedto enviarChatTexto(iachatrequestdto request) {
        return webClient.post()
                .uri("/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(iachatresponsedto.class)
                .block(); // .block() para hacerlo síncrono por ahora
    }

    // CU08: Enviar Audio (Multipart) a FastAPI
    public iachatresponsedto enviarChatVoz(byte[] audioBytes, String filename, iachatrequestdto contextData) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("audio", audioBytes).filename(filename);
        
        try {
            // FastAPI recibe los datos como Form fields, hay que serializar los mapas/listas a JSON
            builder.part("contexto", objectMapper.writeValueAsString(contextData.getContextoDiagrama()));
            builder.part("politica_id", contextData.getPoliticaId());
            builder.part("historial", objectMapper.writeValueAsString(contextData.getHistorial()));
        } catch (Exception e) {
            throw new RuntimeException("Error serializando contexto para IA");
        }

        return webClient.post()
                .uri("/ai/chat/voz")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(iachatresponsedto.class)
                .block();
    }    
}
