from pydantic import BaseModel, Field
from typing import List, Optional, Any, Dict

# Modelo para las acciones individuales que generará la IA
class Accion(BaseModel):
    tipo: str  # AGREGAR_NODO, ELIMINAR_NODO, etc.
    datos: Dict[str, Any]

# Lo que recibe FastAPI desde Spring Boot
class ChatRequest(BaseModel):
    mensaje: str
    contextoDiagrama: Dict[str, Any]  # Coincide con Java
    politicaId: str                  # Coincide con Java
    historial: List[Dict[str, str]]

# Lo que responde FastAPI a Spring Boot
class ChatResponse(BaseModel):
    # 🌟 Acepta 'respuesta_texto' o 'respuestaTexto' en la validación interna, serializa en CamelCase para Java
    respuesta_texto: str = Field(validation_alias="respuesta_texto", serialization_alias="respuestaTexto")
    acciones: list = Field(serialization_alias="acciones")
    
    # 🌟 Acepta variantes idiomáticas de Groq en la inicialización sin romperse
    tiene_cambios: bool = Field(validation_alias="tiene_cambios", serialization_alias="tieneCambios")
    texto_transcrito: Optional[str] = None