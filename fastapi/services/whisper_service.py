import os
from groq import Groq
from services.gemini_service import GeminiService
from schemas.chat_schema import ChatRequest, ChatResponse

class WhisperService:
    def __init__(self):
        self.client = Groq(api_key=os.getenv("GROQ_API_KEY"))
        self.gemini_service = GeminiService()

    async def transcribir_y_procesar(self, audio_file, contexto, politica_id, historial) -> ChatResponse:
        audio_data = await audio_file.read()
        
        # Guardamos temporalmente para Groq
        tmp_filename = f"tmp_{politica_id}.wav"
        with open(tmp_filename, "wb") as f:
            f.write(audio_data)

        try:
            # 1. Transcripción con Whisper en Groq (Gratis y ultra rápido)
            with open(tmp_filename, "rb") as file:
                transcription = self.client.audio.transcriptions.create(
                    file=(tmp_filename, file.read()),
                    model="whisper-large-v3", # El modelo más potente de Whisper
                    language="es",
                    response_format="json"
                )
            
            texto_transcrito = transcription.text

            # 2. Procesar el texto con Gemini (nuestro cerebro gratuito)
            request_gemini = ChatRequest(
                mensaje=texto_transcrito,
                contexto_diagrama=contexto,
                politica_id=politica_id,
                historial=historial
            )
            
            resultado = self.gemini_service.procesar_mensaje_chat(request_gemini)
            resultado.texto_transcrito = texto_transcrito
            return resultado

        finally:
            if os.path.exists(tmp_filename):
                os.remove(tmp_filename)