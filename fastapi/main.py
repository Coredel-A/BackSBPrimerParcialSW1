from dotenv import load_dotenv
load_dotenv()

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from routers import chat


app = FastAPI(title="ProChain AI Service")

# --- AÑADE ESTO PARA DEPURAR EL ERROR 422 ---
@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    print("❌ ERROR DE VALIDACIÓN DETECTADO:")
    print(exc.errors())
    return JSONResponse(
        status_code=422,
        content={"detail": exc.errors(), "body": exc.body},
    )
# ---------------------------------------------

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # Temporalmente "*" para pruebas
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(chat.router, prefix="/ai", tags=["Chat IA"])

@app.get("/")
def read_root():
    return {"status": "AI Service Running"}