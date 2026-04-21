from app.models import IngestRequest


def ingest_document(request: IngestRequest) -> int:
    raise NotImplementedError("Implemented in Task 11")


def delete_document(document_id: str):
    raise NotImplementedError("Implemented in Task 11")
