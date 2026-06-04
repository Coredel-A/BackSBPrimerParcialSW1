import os
import json
import google.generativeai as genai
from schemas.chat_schema import Accion, ChatRequest, ChatResponse
from groq import Groq
from dotenv import load_dotenv

load_dotenv()

google_key = os.getenv("GOOGLE_API_KEY")
os.environ["GOOGLE_API_USE_MTLS"] = "never"
genai.configure(api_key=google_key, transport='rest')

if google_key:
    genai.configure(api_key=google_key)
    print("✅ Gemini configurado globalmente")
else:
    print("❌ Falta GOOGLE_API_KEY")

groq_key = os.getenv("GROQ_API_KEY")
if groq_key:
    client_groq = Groq(api_key=groq_key)
    print("✅ Groq configurado")
else:
    print("❌ Falta GROQ_API_KEY")


def _construir_contexto_enriquecido(contexto: dict) -> str:
    """
    Lee directamente la estructura enriquecida que Spring Boot procesa en 
    obtenerDiagramaParaIA, garantizando sincronía absoluta entre el canvas y el LLM.
    """
    altura_carril = contexto.get("alturaCarril", 150)
    carril_descripcion = contexto.get("carrilDescripcion", [])  # Trae index, id, nombre, yRecomendada
    nodos = contexto.get("nodos", [])
    conexiones = contexto.get("conexiones", [])
    
    # Recuperamos el valor exacto calculado por Spring Boot (fallback a 200 si no viene)
    x_siguiente = contexto.get("xSiguiente", 200.0)

    # ── TABLA DE CARRILES (Mapeo Directo desde Java) ─────────────────
    lineas_carriles = []
    for carril in carril_descripcion:
        if isinstance(carril, dict):
            idx = carril.get("index")
            nombre = carril.get("nombre")
            dept_id = carril.get("id")
            y_centro = carril.get("yRecomendada")
            y_min = carril.get("yMin")
            y_max = carril.get("yMax")
            
            lineas_carriles.append(
                f'  - Índice {idx}: nombre="{nombre}", id="{dept_id}", '
                f'zona_Y=[{y_min}..{y_max}], Y_recomendada_para_nodos={y_centro}'
            )

    tabla_carriles = "\n".join(lineas_carriles) if lineas_carriles else "  (sin carriles definidos)"

    # ── TABLA DE NODOS EXISTENTES ────────────────────────────────────
    lineas_nodos = []
    # Generamos un diccionario rápido id->nombre de carril para los nodos existentes
    mapa_aux_nombres = {c.get("id"): c.get("nombre") for c in carril_descripcion if isinstance(c, dict)}
    
    for n in nodos:
        if not isinstance(n, dict):
            continue
        did = n.get("departamentoId", "")
        nombre_dept = mapa_aux_nombres.get(did, f"ID: {did}")
        pos = n.get("posicion", {})
        lineas_nodos.append(
            f'  - id="{n.get("id")}", nombre="{n.get("nombre")}", tipo={n.get("tipo")}, '
            f'departamento="{nombre_dept}" (id={did}), posicion={{x={pos.get("x")}, y={pos.get("y")}}}'
        )

    tabla_nodos = "\n".join(lineas_nodos) if lineas_nodos else "  (sin nodos)"

    # ── TABLA DE CONEXIONES EXISTENTES ──────────────────────────────
    lineas_conns = []
    for c in conexiones:
        if not isinstance(c, dict):
            continue
        lineas_conns.append(
            f'  - id="{c.get("id")}", {c.get("nodoOrigenId")} → {c.get("nodoDestinoId")}, tipo={c.get("tipoFlujo")}'
        )
    tabla_conns = "\n".join(lineas_conns) if lineas_conns else "  (sin conexiones)"

    return f"""
=== ESTADO ACTUAL DEL DIAGRAMA ===

alturaCarril: {altura_carril}px

CARRILES DISPONIBLES EN EL CANVAS (Usa estos nombres e IDs):
{tabla_carriles}

NODOS EXISTENTES:
{tabla_nodos}

CONEXIONES EXISTENTES:
{tabla_conns}

SIGUIENTE_X_LIBRE: {x_siguiente}
(Usa este valor numérico exacto para la propiedad "x" del primer nodo nuevo que crees. Si crees más de uno en el mismo carril, ve sumando de 150px en 150px)
""".strip()


class GeminiService:
    def __init__(self):
        self.model = genai.GenerativeModel('models/gemini-2.0-flash')
        self.groq_model = "llama-3.3-70b-versatile"

    def procesar_mensaje_chat(self, request: ChatRequest) -> ChatResponse:

        # FIX: Construimos el contexto enriquecido ANTES de enviarlo al LLM
        contexto_legible = _construir_contexto_enriquecido(request.contextoDiagrama)
        altura_carril = request.contextoDiagrama.get("alturaCarril", 150)

        system_prompt = f"""
Eres un asistente experto en modelado de procesos para ProChain.
Tu tarea es modificar diagramas de flujo organizados en carriles horizontales (Swimlanes).

════════════════════════════════════════
REGLAS DE COORDENADAS — LEE CON ATENCIÓN
════════════════════════════════════════

El canvas tiene carriles horizontales apilados verticalmente.
La altura de cada carril es {altura_carril}px.

REGLA DE ORO PARA LA Y:
  Cada carril tiene una "Y_recomendada_para_nodos" en el contexto.
  USA ESE VALOR EXACTO para el campo "y" del nodo.
  NUNCA uses la misma Y para nodos de carriles diferentes.
  NUNCA calcules la Y tú mismo; lee la tabla de carriles del contexto.

REGLA PARA EL departamentoId:
  En la tabla de carriles, cada entrada tiene nombre= e id=.
  Cuando el usuario mencione un departamento por nombre (ej: "Recursos Humanos"),
  busca en la tabla cuál fila tiene ese nombre y usa su id= exacto.
  NUNCA reutilices el departamentoId de un nodo anterior si el usuario pide
  un carril diferente.

REGLA PARA LA X:
  Usa el campo SIGUIENTE_X_LIBRE del contexto como punto de partida.
  Incrementa 150px por cada nuevo nodo que agregues en la misma secuencia.

════════════════════════════════════════
ACCIONES DISPONIBLES
════════════════════════════════════════

1. AGREGAR_NODO   → datos: id (prefijo "temp_"), nombre, tipo, departamentoId, x, y
2. ELIMINAR_NODO  → datos: id
3. ACTUALIZAR_NODO → datos: id + campos a cambiar
4. AGREGAR_CONEXION → datos: source, target, tipo (SECUENCIAL|ALTERNATIVO_SI|ALTERNATIVO_NO|PARALELO)
5. ELIMINAR_CONEXION → datos: id

Tipos de nodo válidos: INICIO, TAREA, DECISION, FIN, PARALELO_SPLIT, PARALELO_JOIN

════════════════════════════════════════
FORMATO DE RESPUESTA (JSON ESTRICTO)
════════════════════════════════════════

Responde ÚNICAMENTE con este JSON, sin texto adicional, sin markdown:
{{
  "respuesta_texto": "Descripción breve de lo que hiciste",
  "acciones": [
    {{
      "tipo": "AGREGAR_NODO",
      "datos": {{
        "id": "temp_nodo_1",
        "nombre": "Nombre del Nodo",
        "tipo": "TAREA",
        "departamentoId": "<id exacto del carril>",
        "x": 200.0,
        "y": <Y_recomendada del carril correspondiente>
      }}
    }}
  ],
  "tiene_cambios": true
}}

NUNCA omitas "departamentoId". NUNCA uses la misma Y para carriles distintos.
"""

        user_content = f"""
{contexto_legible}

Instrucción del usuario: {request.mensaje}
"""

        print(f"\n📋 Contexto enriquecido enviado al LLM:\n{contexto_legible}\n")

        res_data = None

        try:
            response = self.model.generate_content(
                contents=[system_prompt, user_content],
                generation_config={"response_mime_type": "application/json"}
            )
            res_data = json.loads(response.text)
            print(f"✅ Gemini respondió: {json.dumps(res_data, ensure_ascii=False)[:300]}")

        except Exception as e:
            print(f"⚠️ Gemini falló. Usando Groq... (Error: {str(e)[:120]})")
            try:
                completion = client_groq.chat.completions.create(
                    model=self.groq_model,
                    messages=[
                        {"role": "system", "content": system_prompt},
                        {"role": "user",   "content": user_content}
                    ],
                    response_format={"type": "json_object"}
                )
                res_data = json.loads(completion.choices[0].message.content)
                print(f"✅ Groq respondió: {json.dumps(res_data, ensure_ascii=False)[:300]}")

            except Exception as groq_e:
                print(f"❌ Groq también falló: {groq_e}")
                return ChatResponse(
                    respuesta_texto="Lo siento, los servicios de IA están saturados en este momento.",
                    acciones=[],
                    tiene_cambios=False
                )

        # ── NORMALIZACIÓN DE LA RESPUESTA ────────────────────────────
        acciones_procesadas = []
        for item in res_data.get("acciones", res_data.get("actions", [])):
            raw_datos = (
                item.get("datos")
                or item.get("nodo")
                or item.get("conexion")
                or {}
            )

            # Tipo de acción — blindado contra variantes de nombres
            tipo_accion = (
                item.get("tipo")
                or item.get("type")
                or item.get("action")
                or item.get("accion")
                or ""
            )
            tipo_accion = str(tipo_accion).strip().upper()

            # Fallback: si viene con datos de nodo pero sin tipo, asumimos AGREGAR_NODO
            if not tipo_accion and (raw_datos.get("id") or raw_datos.get("nombre")):
                tipo_accion = "AGREGAR_NODO"

            x_val = raw_datos.get("x") or raw_datos.get("posicion", {}).get("x", 100)
            y_val = raw_datos.get("y") or raw_datos.get("posicion", {}).get("y", 100)

            source_node = (
                raw_datos.get("source")
                or raw_datos.get("sourceId")
                or raw_datos.get("nodoOrigenId")
            )
            target_node = (
                raw_datos.get("target")
                or raw_datos.get("targetId")
                or raw_datos.get("nodoDestinoId")
            )

            # FIX: Validación adicional — si el departamentoId que devuelve la IA
            # no existe en el mapa de carriles, intentamos resolverlo por nombre
            dept_id = raw_datos.get("departamentoId")
            contexto_raw = request.contextoDiagrama
            orden_carriles = contexto_raw.get("ordenCarriles", [])
            departamentos = contexto_raw.get("departamentos", [])
            ids_validos = set(orden_carriles)

            if dept_id and dept_id not in ids_validos and departamentos:
                # Intentar resolver por nombre (por si la IA devolvió el nombre en lugar del id)
                for d in departamentos:
                    if isinstance(d, dict):
                        if d.get("nombre", "").lower() == str(dept_id).lower():
                            dept_id = d.get("id", dept_id)
                            print(f"🔧 departamentoId corregido por nombre: '{raw_datos.get('departamentoId')}' → '{dept_id}'")
                            break

            datos_para_java = {
                "id":            raw_datos.get("id"),
                "nombre":        raw_datos.get("nombre"),
                "tipo":          raw_datos.get("tipo"),
                "departamentoId": dept_id,
                "x":             float(x_val),
                "y":             float(y_val),
                "source":        source_node,
                "target":        target_node,
                "tipoFlujo":     raw_datos.get("tipoFlujo") or raw_datos.get("tipo")
            }

            print(f"  → Acción procesada: tipo={tipo_accion}, dept={dept_id}, x={x_val}, y={y_val}")

            acciones_procesadas.append(Accion(tipo=tipo_accion, datos=datos_para_java))

        cambios_detectados = False
        if isinstance(res_data, dict):
            cambios_detectados = (
                res_data.get("tiene_cambios")
                if res_data.get("tiene_cambios") is not None
                else res_data.get("tiene_changes", res_data.get("has_changes", True))
            )

        return ChatResponse(
            respuesta_texto=res_data.get("respuesta_texto") or res_data.get("respuestaTexto") or "Completado",
            acciones=acciones_procesadas,
            tiene_cambios=bool(cambios_detectados)
        )