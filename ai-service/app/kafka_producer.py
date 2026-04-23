import json
from kafka import KafkaProducer
from app.config import KAFKA_BOOTSTRAP_SERVERS
from app.logging_config import logger


_producer = None


def get_producer() -> KafkaProducer:
    global _producer
    if _producer is None:
        _producer = KafkaProducer(
            bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
            value_serializer=lambda v: json.dumps(v).encode("utf-8"),
            key_serializer=lambda k: k.encode("utf-8") if k else None,
            retries=3,
        )
    return _producer


def publish_ingested(document_id: str, status: str, chunk_count: int, error_message: str = None):
    producer = get_producer()
    payload = {
        "documentId": document_id,
        "status": status,
        "chunkCount": chunk_count,
        "errorMessage": error_message,
    }
    producer.send("document.ingested", key=document_id, value=payload)
    producer.flush()
    logger.info("published_ingested_event", document_id=document_id, status=status)
