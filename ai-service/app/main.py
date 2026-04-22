import uuid
from fastapi import FastAPI, HTTPException, Header
from typing import Optional

from app.models import IngestRequest, IngestResponse, ChatRequest, ChatResponse
from app.logging_config import configure_logging, logger
from app import ingest, agent

configure_logging()
app = FastAPI(title="Agentic RAG AI Service")


@app.post("/ingest", response_model=IngestResponse)
async def ingest_document(request: IngestRequest,
                           x_request_id: Optional[str] = Header(None)):
    request_id = x_request_id or str(uuid.uuid4())
    log = logger.bind(request_id=request_id, document_id=request.document_id)
    log.info("ingest_started", filename=request.filename)
    try:
        chunk_count = ingest.ingest_document(request)
        log.info("ingest_complete", chunk_count=chunk_count)
        return IngestResponse(document_id=request.document_id,
                              chunk_count=chunk_count, status="READY")
    except Exception as e:
        log.error("ingest_failed", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))


@app.delete("/document/{document_id}")
async def delete_document(document_id: str,
                           x_request_id: Optional[str] = Header(None)):
    request_id = x_request_id or str(uuid.uuid4())
    logger.bind(request_id=request_id).info("delete_document", document_id=document_id)
    ingest.delete_document(document_id)
    return {"status": "deleted"}


@app.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest,
               x_request_id: Optional[str] = Header(None)):
    request_id = x_request_id or str(uuid.uuid4())
    log = logger.bind(request_id=request_id, user_id=request.user_id)
    log.info("chat_started", message_length=len(request.message),
             document_ids=request.document_ids)
    result = agent.run_agent(request, request_id)
    log.info("chat_complete", iterations=result["iterations_used"])
    return ChatResponse(**result)
