from pydantic import BaseModel


class IngestRequest(BaseModel):
    document_id: str
    user_id: str
    filename: str
    file_bytes: str  # base64 encoded


class IngestResponse(BaseModel):
    document_id: str
    chunk_count: int
    status: str


class ChatMessage(BaseModel):
    role: str  # "user" | "assistant"
    content: str


class ChatRequest(BaseModel):
    message: str
    user_id: str
    conversation_history: list[ChatMessage]
    document_ids: list[str]


class ChatResponse(BaseModel):
    answer: str
    sources: list[dict]
    iterations_used: int
