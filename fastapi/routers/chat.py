from fastapi import APIRouter, UploadFile, File, Form
from schemas.chat_schema import ChatRequest, ChatResponse
from services.gemini_service import GeminiService # Cambiado
# Si decides usar Whisper local, lo importas aquí
import json

router = APIRouter()
gemini_service = GeminiService()

@router.post("/chat", response_model=ChatResponse)
async def chat_texto(request: ChatRequest):
    return gemini_service.procesar_mensaje_chat(request)