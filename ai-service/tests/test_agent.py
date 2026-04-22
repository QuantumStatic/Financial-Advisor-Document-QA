from unittest.mock import patch, MagicMock
from app.models import ChatRequest, ChatMessage


def make_chat_request():
    return ChatRequest(
        message="What is the portfolio value?",
        user_id="user-456",
        conversation_history=[],
        document_ids=["doc-123"]
    )


def test_run_agent_returns_answer():
    from app import agent
    with patch("app.agent.get_chroma_collection") as mock_col, \
         patch("app.agent.ChatGroq") as mock_groq:

        mock_collection = MagicMock()
        mock_collection.query.return_value = {
            "documents": [["The portfolio is worth $500,000"]],
            "metadatas": [[{"filename": "portfolio.pdf", "page": 1, "document_id": "doc-123", "user_id": "user-456"}]]
        }
        mock_col.return_value = mock_collection

        mock_llm = MagicMock()
        mock_llm.invoke.return_value = MagicMock(content="YES")
        mock_groq.return_value = mock_llm

        result = agent.run_agent(make_chat_request(), "req-123")
        assert "answer" in result
        assert result["iterations_used"] >= 1
        assert isinstance(result["sources"], list)
