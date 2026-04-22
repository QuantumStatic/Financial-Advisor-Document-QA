from typing import TypedDict
from langgraph.graph import StateGraph, END
from langchain_groq import ChatGroq

from app.config import GROQ_API_KEY, GROQ_MODEL, MAX_ITERATIONS
from app.ingest import get_chroma_collection
from app.logging_config import logger
from app.models import ChatRequest


class AgentState(TypedDict):
    message: str
    user_id: str
    document_ids: list[str]
    conversation_history: list[dict]
    retrieved_chunks: list[dict]
    query: str
    iterations: int
    answer: str
    sufficient: bool


def retrieve_chunks(state: AgentState) -> AgentState:
    collection = get_chroma_collection()
    query = state.get("query") or state["message"]

    doc_ids = state["document_ids"]
    if len(doc_ids) == 1:
        where_filter = {
            "$and": [
                {"document_id": {"$eq": doc_ids[0]}},
                {"user_id": {"$eq": state["user_id"]}}
            ]
        }
    else:
        where_filter = {
            "$and": [
                {"document_id": {"$in": doc_ids}},
                {"user_id": {"$eq": state["user_id"]}}
            ]
        }

    results = collection.query(
        query_texts=[query], n_results=5, where=where_filter
    )
    chunks = []
    for doc, meta in zip(results["documents"][0], results["metadatas"][0]):
        chunks.append({
            "text": doc,
            "filename": meta.get("filename"),
            "page": meta.get("page")
        })

    logger.info("retrieved_chunks", iteration=state["iterations"] + 1, count=len(chunks))
    return {**state, "retrieved_chunks": chunks, "iterations": state["iterations"] + 1}


def assess_sufficiency(state: AgentState) -> AgentState:
    if state["iterations"] >= MAX_ITERATIONS or not state["document_ids"]:
        return {**state, "sufficient": True}
    llm = ChatGroq(api_key=GROQ_API_KEY, model=GROQ_MODEL, temperature=0)
    context = "\n".join(c["text"] for c in state["retrieved_chunks"])
    prompt = (
        f'Given this context:\n{context}\n\n'
        f'Can you fully answer: "{state["message"]}"?\n'
        f'Reply with only YES or NO.'
    )
    response = llm.invoke(prompt)
    sufficient = "YES" in response.content.upper()
    return {**state, "sufficient": sufficient}


def refine_query(state: AgentState) -> AgentState:
    llm = ChatGroq(api_key=GROQ_API_KEY, model=GROQ_MODEL, temperature=0)
    prompt = (
        f'Original question: {state["message"]}\n'
        f'Current search query: {state.get("query", state["message"])}\n'
        f'The retrieved context was insufficient. Generate a different, more specific search query.\n'
        f'Reply with only the new query.'
    )
    response = llm.invoke(prompt)
    return {**state, "query": response.content.strip()}


def generate_answer(state: AgentState) -> AgentState:
    llm = ChatGroq(api_key=GROQ_API_KEY, model=GROQ_MODEL, temperature=0)
    context = "\n\n".join(
        f"[{c['filename']} p.{c['page']}]: {c['text']}"
        for c in state["retrieved_chunks"]
    )
    history = "\n".join(
        f"{m['role'].capitalize()}: {m['content']}"
        for m in state["conversation_history"][-6:]
    )
    prompt = (
        f"You are a financial advisor assistant.\n\n"
        f"Conversation history:\n{history}\n\n"
        f"Retrieved context:\n{context}\n\n"
        f"Answer the question: {state['message']}\n"
        f"Be concise and cite the document filename when relevant."
    )
    response = llm.invoke(prompt)
    return {**state, "answer": response.content}


def should_continue(state: AgentState) -> str:
    if state["sufficient"] or state["iterations"] >= MAX_ITERATIONS:
        return "generate"
    return "refine"


def build_graph():
    graph = StateGraph(AgentState)
    graph.add_node("retrieve", retrieve_chunks)
    graph.add_node("assess", assess_sufficiency)
    graph.add_node("refine", refine_query)
    graph.add_node("generate", generate_answer)

    graph.set_entry_point("retrieve")
    graph.add_edge("retrieve", "assess")
    graph.add_conditional_edges("assess", should_continue, {
        "generate": "generate",
        "refine": "refine"
    })
    graph.add_edge("refine", "retrieve")
    graph.add_edge("generate", END)
    return graph.compile()


_graph = build_graph()


def run_agent(request: ChatRequest, request_id: str) -> dict:
    state = AgentState(
        message=request.message,
        user_id=request.user_id,
        document_ids=request.document_ids,
        conversation_history=[m.model_dump() for m in request.conversation_history],
        retrieved_chunks=[],
        query=request.message,
        iterations=0,
        answer="",
        sufficient=False
    )
    result = _graph.invoke(state)
    sources = result["retrieved_chunks"][:3]
    return {
        "answer": result["answer"],
        "sources": sources,
        "iterations_used": result["iterations"]
    }
