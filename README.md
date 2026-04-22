# Agentic RAG — Spring Boot + LangGraph

A document Q&A system with an agentic retrieval loop. Upload PDFs, ask questions, get answers grounded in your documents with source citations.

## Stack

- **Backend** — Spring Boot 3 (auth, document management, conversation history, AI proxy)
- **AI Service** — Python FastAPI + LangGraph (iterative RAG with up to 10 retrieval refinement steps)
- **Vector Store** — ChromaDB
- **LLM** — Groq (`llama-3.3-70b-versatile`)
- **Frontend** — React + Vite + Tailwind CSS (served from Spring Boot)
- **Database** — PostgreSQL (Flyway migrations)

## How it works

The AI service runs a LangGraph `StateGraph`: retrieve → assess sufficiency → refine query (loop, max 10×) → generate answer. Each answer includes source citations (filename + page).

## Running locally

```bash
cp .env.example .env
# fill in GROQ_API_KEY and JWT_SECRET
docker compose up --build
```

App available at `http://localhost:8080`.

## Development

```bash
# Backend
cd backend && mvn spring-boot:run

# AI service
cd ai-service && pip install -r requirements.txt && uvicorn app.main:app --reload

# Frontend (proxies /api to localhost:8080)
cd frontend && npm install && npm run dev
```
