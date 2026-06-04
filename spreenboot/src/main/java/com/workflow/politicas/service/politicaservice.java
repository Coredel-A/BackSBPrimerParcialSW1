package com.workflow.politicas.service;

import com.workflow.politicas.model.politicas;
import com.workflow.politicas.model.nodo;
import com.workflow.politicas.model.conexion;
import com.workflow.politicas.model.departamento;
import com.workflow.politicas.repository.politicanegociorepository;
import com.workflow.politicas.repository.departamentorepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.workflow.politicas.dto.iaacciondto;
import com.workflow.politicas.dto.posicionupdatedto;

@Service
public class politicaservice {
    private final politicanegociorepository repository;

    // FIX: Inyectamos el repositorio de departamentos para poder incluir
    // sus nombres en el contexto que enviamos a la IA.
    private final departamentorepository departamentoRepository;

    public politicaservice(politicanegociorepository repository,
                           departamentorepository departamentoRepository) {
        this.repository = repository;
        this.departamentoRepository = departamentoRepository;
    }

    public List<politicas> listarTodas() {
        return repository.findAll();
    }

    public politicas crear(politicas politica) {
        politica.setCreatedAt(LocalDateTime.now());
        politica.setUpdatedAt(LocalDateTime.now());
        if (politica.getNodos() == null) politica.setNodos(new ArrayList<>());
        if (politica.getConexiones() == null) politica.setConexiones(new ArrayList<>());
        if (politica.getOrdenCarriles() == null) politica.setOrdenCarriles(new ArrayList<>());
        if (politica.getAlturaCarril() <= 0) politica.setAlturaCarril(150);
        if (politica.getEstado() == null) politica.setEstado("BORRADOR");
        return repository.save(politica);
    }

    public politicas buscarPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Política no encontrada"));
    }

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
        p.getNodos().removeIf(n -> n.getId().equals(nodoId));
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
                if (n.getPosicion() == null) {
                    n.setPosicion(new java.util.HashMap<>());
                }
                n.getPosicion().put("x", pos.getX());
                n.getPosicion().put("y", pos.getY());
                if (pos.getDepartamentoId() != null && !pos.getDepartamentoId().isEmpty()) {
                    n.setDepartamentoId(pos.getDepartamentoId());
                }
            });
        return repository.save(p);
    }

    public politicas agregarConexion(String politicaId, conexion conexion) {
        politicas p = buscarPorId(politicaId);

        boolean existeOrigen  = p.getNodos().stream().anyMatch(n -> n.getId().equals(conexion.getNodoOrigenId()));
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

    public politicas reordenarCarril(String politicaId, String departamentoId, int nuevaPosicion) {
        politicas p = buscarPorId(politicaId);
        verificarSiEsEditable(p);
        List<String> orden = p.getOrdenCarriles();
        if (orden.contains(departamentoId)) {
            orden.remove(departamentoId);
            if (nuevaPosicion < 0) nuevaPosicion = 0;
            if (nuevaPosicion > orden.size()) nuevaPosicion = orden.size();
            orden.add(nuevaPosicion, departamentoId);
            p.setOrdenCarriles(orden);
            p.setUpdatedAt(LocalDateTime.now());
            return repository.save(p);
        } else {
            throw new RuntimeException("El departamento no está registrado como carril en esta política.");
        }
    }

    public politicas ajustarAlturaCarril(String politicaId, int nuevaAltura) {
        if (nuevaAltura < 100 || nuevaAltura > 400) {
            throw new IllegalArgumentException("La altura del carril debe estar entre 100 y 400 píxeles.");
        }
        politicas p = buscarPorId(politicaId);
        verificarSiEsEditable(p);
        p.setAlturaCarril(nuevaAltura);
        p.setUpdatedAt(LocalDateTime.now());
        return repository.save(p);
    }

    public void validarDiagramaCompleto(String politicaId) {
        politicas p = buscarPorId(politicaId);
        List<nodo> nodos = p.getNodos();
        List<conexion> conexiones = p.getConexiones();
        List<String> ordenCarriles = p.getOrdenCarriles();

        for (nodo n : nodos) {
            if (n.getDepartamentoId() == null || n.getDepartamentoId().isEmpty()) {
                throw new RuntimeException("El nodo '" + n.getNombre() + "' no tiene un departamento/carril asignado.");
            }
            if (!ordenCarriles.contains(n.getDepartamentoId())) {
                throw new RuntimeException("El nodo '" + n.getNombre() + "' está asignado a un carril inexistente en la política.");
            }
        }

        long conteoInicio = nodos.stream().filter(n -> "INICIO".equals(n.getTipo())).count();
        if (conteoInicio != 1) {
            throw new RuntimeException("La política debe tener exactamente UN nodo de inicio.");
        }

        long conteoFin = nodos.stream().filter(n -> "FIN".equals(n.getTipo())).count();
        if (conteoFin < 1) {
            throw new RuntimeException("La política debe tener al menos un nodo de fin.");
        }

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

    // ────────────────────────────────────────────────────────────────
    //  CONTEXTO PARA LA IA — FIX PRINCIPAL
    // ────────────────────────────────────────────────────────────────

    /**
     * FIX: Se agrega el campo "departamentos" con id+nombre al contexto.
     *
     * Antes, el contexto solo contenía IDs opacos en "ordenCarriles":
     *   ["69e938b4...", "6a148514...", ...]
     *
     * El LLM no podía resolver "carril de Recursos Humanos" → ID correcto,
     * así que reutilizaba el departamentoId del último nodo que había visto,
     * poniendo todos los nodos nuevos en el mismo carril.
     *
     * Ahora el contexto incluye:
     *   "departamentos": [{"id": "69e938b4...", "nombre": "Recursos humanos"}, ...]
     *   "carrilDescripcion": [{"index": 0, "id": "...", "nombre": "...", "yRecomendada": 55}, ...]
     *
     * Con esto el LLM puede resolver nombres → IDs y calcular la Y correcta
     * para cada carril sin ambigüedad.
     */
    public Map<String, Object> obtenerDiagramaParaIA(String politicaId) {
        politicas politica = repository.findById(politicaId)
                .orElseThrow(() -> new RuntimeException("Política no encontrada"));

        List<String> ordenCarriles = politica.getOrdenCarriles() != null
                ? politica.getOrdenCarriles()
                : new ArrayList<>();

        int alturaCarril = politica.getAlturaCarril() > 0 ? politica.getAlturaCarril() : 150;

        // ── 1. Cargar todos los departamentos de la BD ───────────────
        List<departamento> todosLosDepartamentos = departamentoRepository.findAll();

        // ── 2. Construir mapa id → nombre para lookup rápido ────────
        Map<String, String> mapaNombres = todosLosDepartamentos.stream()
                .filter(d -> d.getId() != null)
                .collect(Collectors.toMap(
                        departamento::getId,
                        departamento::getNombre,
                        (a, b) -> a   // en caso de duplicado, quedarse con el primero
                ));

        // ── 3. Lista plana de departamentos (id + nombre) ───────────
        // Esto permite que el LLM resuelva "Recursos Humanos" → ID correcto
        List<Map<String, String>> departamentosParaIA = todosLosDepartamentos.stream()
                .map(d -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("id",     d.getId());
                    m.put("nombre", d.getNombre());
                    return m;
                })
                .collect(Collectors.toList());

        // ── 4. Descripción detallada de cada carril con geometría ────
        // Esto es lo más importante: el LLM ve exactamente qué Y usar
        // para cada carril sin tener que calcularlo él mismo (donde fallaba).
        List<Map<String, Object>> carrilDescripcion = new ArrayList<>();
        for (int i = 0; i < ordenCarriles.size(); i++) {
            String deptId = ordenCarriles.get(i);
            String nombre = mapaNombres.getOrDefault(deptId, "Departamento " + i);
            int yRecomendada = (i * alturaCarril) + (alturaCarril / 2) - 20;

            Map<String, Object> infoCarril = new HashMap<>();
            infoCarril.put("index",        i);
            infoCarril.put("id",           deptId);
            infoCarril.put("nombre",       nombre);
            infoCarril.put("yMin",         i * alturaCarril);
            infoCarril.put("yMax",         (i + 1) * alturaCarril);
            infoCarril.put("yRecomendada", yRecomendada);
            carrilDescripcion.add(infoCarril);
        }

        // ── 5. X siguiente disponible ────────────────────────────────
        // Evita que la IA ponga nuevos nodos encima de los existentes
        double xMaxima = politica.getNodos().stream()
                .filter(n -> n.getPosicion() != null && n.getPosicion().get("x") != null)
                .mapToDouble(n -> ((Number) n.getPosicion().get("x")).doubleValue())
                .max()
                .orElse(50.0);
        double xSiguiente = xMaxima + 150.0;

        // ── 6. Construir el mapa final del contexto ──────────────────
        Map<String, Object> contexto = new HashMap<>();
        contexto.put("nodos",             politica.getNodos());
        contexto.put("conexiones",        politica.getConexiones());
        contexto.put("ordenCarriles",     ordenCarriles);
        contexto.put("alturaCarril",      alturaCarril);

        // NUEVOS campos que la IA necesita para operar correctamente:
        contexto.put("departamentos",     departamentosParaIA);   // id + nombre de cada depto
        contexto.put("carrilDescripcion", carrilDescripcion);      // geometría completa por carril
        contexto.put("xSiguiente",        xSiguiente);            // dónde colocar el próximo nodo

        System.out.println("Contexto para IA generado: carriles=" + carrilDescripcion.size()
                + ", nodos=" + politica.getNodos().size()
                + ", xSiguiente=" + xSiguiente);

        return contexto;
    }

    // ────────────────────────────────────────────────────────────────
    //  ACCIONES DE LA IA
    // ────────────────────────────────────────────────────────────────

    @Transactional
    public void aplicarAccionesIA(String politicaId, List<iaacciondto> acciones) {
        for (iaacciondto accion : acciones) {
            Map<String, Object> datos = accion.getDatos();
            switch (accion.getTipo().toUpperCase()) {
                case "AGREGAR_NODO":
                    this.agregarNodoDesdeIA(politicaId, datos);
                    break;
                case "ELIMINAR_NODO":
                    this.eliminarNodo(politicaId, (String) datos.get("id"));
                    break;
                case "ACTUALIZAR_NODO":
                    this.actualizarNodoDesdeIA(politicaId, datos);
                    break;
                case "AGREGAR_CONEXION":
                    this.conectarNodos(
                        politicaId,
                        (String) datos.get("source"),
                        (String) datos.get("target"),
                        (String) datos.get("tipo")
                    );
                    break;
                case "ELIMINAR_CONEXION":
                    this.eliminarConexion(politicaId, (String) datos.get("id"));
                    break;
                default:
                    System.out.println("⚠️ Acción de IA desconocida ignorada: " + accion.getTipo());
            }
        }
    }

    private void actualizarNodoDesdeIA(String politicaId, Map<String, Object> datos) {
        String nodoId = (String) datos.get("id");
        nodo nodoUpdate = new nodo();
        nodoUpdate.setNombre((String) datos.get("nombre"));
        nodoUpdate.setTipo((String) datos.get("tipo"));
        nodoUpdate.setDepartamentoId((String) datos.get("departamentoId"));
        nodoUpdate.setDescripcion((String) datos.get("descripcion"));
        this.actualizarNodo(politicaId, nodoId, nodoUpdate);
    }

    public void conectarNodos(String politicaId, String sourceId, String targetId, String tipoFlujo) {
        if (sourceId == null || targetId == null || sourceId.isEmpty() || targetId.isEmpty()) {
            throw new IllegalArgumentException("La IA no logró identificar los IDs de los nodos para conectarlos.");
        }
        conexion nuevaConexion = new conexion();
        nuevaConexion.setNodoOrigenId(sourceId);
        nuevaConexion.setNodoDestinoId(targetId);
        nuevaConexion.setTipoFlujo(tipoFlujo != null ? tipoFlujo : "SECUENCIAL");
        this.agregarConexion(politicaId, nuevaConexion);
    }

    private void agregarNodoDesdeIA(String politicaId, Map<String, Object> datos) {
        nodo nuevoNodo = new nodo();

        String idIA = (String) datos.get("id");
        if (idIA != null && !idIA.isEmpty()) {
            nuevoNodo.setId(idIA);
        }

        String nombreRecibido = (String) datos.get("nombre");
        String tipoNodo       = (String) datos.get("tipo");

        if (nombreRecibido == null || nombreRecibido.trim().isEmpty()) {
            if ("INICIO".equalsIgnoreCase(tipoNodo))      nuevoNodo.setNombre("Inicio");
            else if ("FIN".equalsIgnoreCase(tipoNodo))    nuevoNodo.setNombre("Fin");
            else                                           nuevoNodo.setNombre("Tarea IA");
        } else {
            nuevoNodo.setNombre(nombreRecibido);
        }
        

        nuevoNodo.setTipo(tipoNodo != null ? tipoNodo.toUpperCase() : "TAREA");
        nuevoNodo.setDepartamentoId((String) datos.get("departamentoId"));
        nuevoNodo.setDescripcion((String) datos.get("descripcion"));

        double x = Double.parseDouble(datos.getOrDefault("x", 100).toString());
        double y = Double.parseDouble(datos.getOrDefault("y", 100).toString());
        if (nuevoNodo.getDepartamentoId() != null) {

            politicas p = buscarPorId(politicaId);

            int indiceCarril =
                p.getOrdenCarriles().indexOf(nuevoNodo.getDepartamentoId());

            if (indiceCarril >= 0) {
                int altura = p.getAlturaCarril();

                // posición relativa dentro del carril
                y = (altura / 2.0) - 20;
            }
        }
        Map<String, Double> mapaPos = new HashMap<>();
        mapaPos.put("x", x);
        mapaPos.put("y", y);
        nuevoNodo.setPosicion(mapaPos);

        this.agregarNodo(politicaId, nuevoNodo);
    }

    public politicas guardarOrdenCarriles(String politicaId, List<String> nuevoOrden) {
        politicas p = buscarPorId(politicaId);
        verificarSiEsEditable(p);
        p.setOrdenCarriles(nuevoOrden != null ? nuevoOrden : new ArrayList<>());
        p.setUpdatedAt(LocalDateTime.now());
        return repository.save(p);
    }
}