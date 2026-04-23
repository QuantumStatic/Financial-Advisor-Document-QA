import json
import os
import threading
from kafka import KafkaConsumer
from kafka.errors import NoBrokersAvailable
import time

from app.config import KAFKA_BOOTSTRAP_SERVERS
from app.ingest import ingest_document_from_path
from app.kafka_producer import publish_ingested
from app.logging_config import logger


def _consume_loop():
    # Retry connecting to Kafka — broker may not be ready immediately
    consumer = None
    for attempt in range(10):
        try:
            consumer = KafkaConsumer(
                "document.ingest",
                bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
                group_id="rag-ai-service",
                value_deserializer=lambda m: json.loads(m.decode("utf-8")),
                auto_offset_reset="earliest",
                enable_auto_commit=True,
            )
            logger.info("kafka_consumer_connected")
            break
        except NoBrokersAvailable:
            logger.warning("kafka_not_ready", attempt=attempt + 1)
            time.sleep(5)

    if consumer is None:
        logger.error("kafka_consumer_failed_to_connect")
        return

    for message in consumer:
        event = message.value
        document_id = event.get("documentId", "unknown")
        file_path = event.get("filePath")
        log = logger.bind(document_id=document_id, request_id=event.get("requestId"))
        log.info("ingest_event_received", filename=event.get("filename"))
        try:
            chunk_count = ingest_document_from_path(event)
            # Clean up the file from shared volume after ingestion
            if file_path and os.path.exists(file_path):
                os.remove(file_path)
            publish_ingested(document_id, "READY", chunk_count)
            log.info("ingest_complete", chunk_count=chunk_count)
        except Exception as e:
            log.error("ingest_failed", error=str(e))
            publish_ingested(document_id, "FAILED", 0, str(e))


def start_consumer():
    """Start the Kafka consumer in a background daemon thread."""
    thread = threading.Thread(target=_consume_loop, daemon=True, name="kafka-consumer")
    thread.start()
    logger.info("kafka_consumer_thread_started")
