import base64
import os
import tempfile

import chromadb
from langchain_community.document_loaders import PyPDFLoader
from langchain.text_splitter import RecursiveCharacterTextSplitter

from app.config import CHROMA_HOST, CHROMA_PORT, CHROMA_COLLECTION, CHUNK_SIZE, CHUNK_OVERLAP
from app.models import IngestRequest


def get_chroma_collection():
    client = chromadb.HttpClient(host=CHROMA_HOST, port=CHROMA_PORT)
    return client.get_or_create_collection(CHROMA_COLLECTION)


def ingest_document(request: IngestRequest) -> int:
    file_bytes = base64.b64decode(request.file_bytes)
    with tempfile.NamedTemporaryFile(suffix=".pdf", delete=False) as tmp:
        tmp.write(file_bytes)
        tmp_path = tmp.name

    try:
        loader = PyPDFLoader(tmp_path)
        docs = loader.load()
        splitter = RecursiveCharacterTextSplitter(
            chunk_size=CHUNK_SIZE, chunk_overlap=CHUNK_OVERLAP
        )
        chunks = splitter.split_documents(docs)

        collection = get_chroma_collection()
        ids = [f"{request.document_id}-{i}" for i in range(len(chunks))]
        texts = [c.page_content for c in chunks]
        metadatas = [
            {
                "document_id": request.document_id,
                "user_id": request.user_id,
                "filename": request.filename,
                "page": c.metadata.get("page", 0),
            }
            for c in chunks
        ]
        collection.add(ids=ids, documents=texts, metadatas=metadatas)
        return len(chunks)
    finally:
        os.unlink(tmp_path)


def delete_document(document_id: str):
    collection = get_chroma_collection()
    results = collection.get(where={"document_id": {"$eq": document_id}})
    if results["ids"]:
        collection.delete(ids=results["ids"])
