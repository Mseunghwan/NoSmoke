from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()

class ChatRequest(BaseModel):
    user_message: str

@app.get("/")
def read_root():
    return {"status": "AI Server is running"}

@app.post("/chat")
def chat(request: ChatRequest):
    # 여기에 LangChain 로직이 들어갑니다.
    return {"reply": f"AI 응답: {request.user_message}에 대한 답변입니다."}