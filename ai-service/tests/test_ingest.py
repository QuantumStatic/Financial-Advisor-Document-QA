import base64
from unittest.mock import patch, MagicMock
from app.models import IngestRequest


def make_request():
    fake_pdf = b"%PDF-1.4 stub content"
    return IngestRequest(
        document_id="doc-123",
        user_id="user-456",
        filename="test.pdf",
        file_bytes=base64.b64encode(fake_pdf).decode()
    )


def test_ingest_returns_chunk_count():
    from app import ingest
    with patch("app.ingest.get_chroma_collection") as mock_col, \
         patch("app.ingest.PyPDFLoader") as mock_loader, \
         patch("app.ingest.RecursiveCharacterTextSplitter") as mock_splitter:

        mock_doc = MagicMock()
        mock_doc.page_content = "some text"
        mock_doc.metadata = {"page": 0}
        mock_loader.return_value.load.return_value = [mock_doc]

        mock_chunk = MagicMock()
        mock_chunk.page_content = "chunk text"
        mock_chunk.metadata = {"page": 0}
        mock_splitter.return_value.split_documents.return_value = [mock_chunk]

        mock_collection = MagicMock()
        mock_col.return_value = mock_collection

        count = ingest.ingest_document(make_request())
        assert count == 1
        mock_collection.add.assert_called_once()


def test_delete_document_removes_chunks():
    from app import ingest
    with patch("app.ingest.get_chroma_collection") as mock_col:
        mock_collection = MagicMock()
        mock_collection.get.return_value = {"ids": ["doc-123-0", "doc-123-1"]}
        mock_col.return_value = mock_collection

        ingest.delete_document("doc-123")
        mock_collection.delete.assert_called_once_with(ids=["doc-123-0", "doc-123-1"])
