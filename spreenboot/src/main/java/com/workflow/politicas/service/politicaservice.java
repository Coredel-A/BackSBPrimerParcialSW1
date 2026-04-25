package com.workflow.politicas.service;

import com.workflow.politicas.model.politicas;
import com.workflow.politicas.model.nodo;
import com.workflow.politicas.model.conexion;
import com.workflow.politicas.repository.politicanegociorepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.workflow.politicas.dto.posicionupdatedto;

@Service
public class politicaservice {
    private final politicanegociorepository repository;

    public politicaservice(politicanegociorepository repository) {
        this.repository = repository;
    }

    public List<politicas> listarTodas() {
        return repository.findAll();
    }

    public politicas crear(politicas politica) {
        politica.setCreatedAt(LocalDateTime.now());
        politica.setUpdatedAt(LocalDateTime.now());
        // Inicializamos listas si vienen nulas
        if (politica.getNodos() == null) politica.setNodos(new ArrayList<>());
        if (politica.getConexiones() == null) politica.setConexiones(new ArrayList<>());
        if (politica.getEstado() == null) politica.setEstado("BORRADOR");
        
        return repository.save(politica);
    }

    public politicas buscarPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Política no encontrada"));
    }

    // Lógica para actualizar nodos (muy útil para el editor visual que haremos después)
    public politicas actualizarNodos(String id, List<nodo> nuevosNodos) {
        
        politicas politica = buscarPorId(id);
        politica.setNodos(nuevosNodos);
        politica.setUpdatedAt(LocalDateTime.now());
        return repository.save(politica);
    }

    public void eliminar(String id) {
        repository.deleteById(id);
    }    
    public politicas agregarNodo(String politicaId, nodo nodo) {
        politicas p = buscarPorId(politicaId);
        // Generar ID único si no existe
        if (nodo.getId() == null || nodo.getId().isEmpty()) {
            nodo.setId("node_" + UUID.randomUUID().toString().substring(0, 8));
        }
        p.getNodos().add(nodo);
        p.setUpdatedAt(LocalDateTime.now());
        return repository.save(p);
    }

    public politicas actualizarNodo(String politicaId, String nodoId, nodo nodoActualizado) {
        politicas p = buscarPorId(politicaId);
        verificarSiEsEditable(p);
        p.getNodos().stream()
            .filter(n -> n.getId().equals(nodoId))
            .findFirst()
            .ifPresent(n -> {
                n.setNombre(nodoActualizado.getNombre());
                n.setDescripcion(nodoActualizado.getDescripcion());
                n.setDepartamentoId(nodoActualizado.getDepartamentoId());
                n.setTipo(nodoActualizado.getTipo());
                n.setCondicion(nodoActualizado.getCondicion());
            });
        p.setUpdatedAt(LocalDateTime.now());
        return repository.save(p);
    }

    public politicas eliminarNodo(String politicaId, String nodoId) {
        politicas p = buscarPorId(politicaId);
        verificarSiEsEditable(p);
        // 1. Eliminar el nodo
        p.getNodos().removeIf(n -> n.getId().equals(nodoId));
        
        // 2. IMPORTANTE: Eliminar conexiones vinculadas a este nodo
        p.getConexiones().removeIf(c -> 
            c.getNodoOrigenId().equals(nodoId) || c.getNodoDestinoId().equals(nodoId)
        );
        
        p.setUpdatedAt(LocalDateTime.now());
        return repository.save(p);
    }

    public politicas moverNodo(String politicaId, String nodoId, posicionupdatedto pos) {
        politicas p = buscarPorId(politicaId);
        p.getNodos().stream()
            .filter(n -> n.getId().equals(nodoId))
            .findFirst()
            .ifPresent(n -> {
                n.getPosicion().put("x", pos.getX());
                n.getPosicion().put("y", pos.getY());
            });
        // Nota: Aquí no actualizamos 'updatedAt' para no saturar la BD en cada movimiento
        return repository.save(p);
    }

    public politicas agregarConexion(String politicaId, conexion conexion) {
        politicas p = buscarPorId(politicaId);
        
        // Validar que ambos nodos existan
        boolean existeOrigen = p.getNodos().stream().anyMatch(n -> n.getId().equals(conexion.getNodoOrigenId()));
        boolean existeDestino = p.getNodos().stream().anyMatch(n -> n.getId().equals(conexion.getNodoDestinoId()));
        
        if (!existeOrigen || !existeDestino) {
            throw new RuntimeException("Nodos de conexión no válidos en la política");
        }

        if (conexion.getId() == null || conexion.getId().isEmpty()) {
            conexion.setId("conn_" + UUID.randomUUID().toString().substring(0, 8));
        }
        
        p.getConexiones().add(conexion);
        return repository.save(p);
    }

    public void eliminarConexion(String politicaId, String conexionId) {
        politicas p = buscarPorId(politicaId);
        p.getConexiones().removeIf(c -> c.getId().equals(conexionId));
        repository.save(p);
    }

    public politicas guardarDiagramaCompleto(String politicaId, List<nodo> nodos, List<conexion> conexiones) {
        politicas p = buscarPorId(politicaId);
        verificarSiEsEditable(p);
        p.setNodos(nodos);
        p.setConexiones(conexiones);
        p.setUpdatedAt(LocalDateTime.now());
        return repository.save(p);
    }

    public void validarDiagramaCompleto(String politicaId) {
        politicas p = buscarPorId(politicaId);
        List<nodo> nodos = p.getNodos();
        List<conexion> conexiones = p.getConexiones();

        // 1. Regla: Exactamente un nodo de INICIO
        long conteoInicio = nodos.stream().filter(n -> "INICIO".equals(n.getTipo())).count();
        if (conteoInicio != 1) {
            throw new RuntimeException("La política debe tener exactamente UN nodo de inicio.");
        }

        // 2. Regla: Al menos un nodo de FIN
        long conteoFin = nodos.stream().filter(n -> "FIN".equals(n.getTipo())).count();
        if (conteoFin < 1) {
            throw new RuntimeException("La política debe tener al menos un nodo de fin.");
        }

        // 3. Regla: Nodos de DECISION con salidas válidas
        for (nodo nodo : nodos) {
            if ("DECISION".equals(nodo.getTipo())) {
                long salidas = conexiones.stream()
                    .filter(c -> c.getNodoOrigenId().equals(nodo.getId())).count();
                
                boolean tieneSi = conexiones.stream()
                    .anyMatch(c -> c.getNodoOrigenId().equals(nodo.getId()) && "ALTERNATIVO_SI".equals(c.getTipoFlujo()));
                boolean tieneNo = conexiones.stream()
                    .anyMatch(c -> c.getNodoOrigenId().equals(nodo.getId()) && "ALTERNATIVO_NO".equals(c.getTipoFlujo()));

                if (salidas < 2 || !tieneSi || !tieneNo) {
                    throw new RuntimeException("El nodo de decisión '" + nodo.getNombre() + "' debe tener una salida 'SI' y una 'NO'.");
                }
            }
        }
    }

    private void verificarSiEsEditable(politicas p) {
        if ("ACTIVA".equals(p.getEstado())) {
            throw new RuntimeException("No se puede modificar una política que ya está en estado ACTIVA.");
        }
    }
    public politicas actualizarDiagramaCompleto(String id, List<nodo> nuevosNodos, List<conexion> nuevasConexiones) {
        politicas p = buscarPorId(id);
        p.setNodos(nuevosNodos);
        p.setConexiones(nuevasConexiones);
        return repository.save(p);
    }
}
