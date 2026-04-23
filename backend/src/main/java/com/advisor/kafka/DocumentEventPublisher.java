package com.advisor.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentEventPublisher {

    private final KafkaTemplate<String, DocumentIngestEvent> kafkaTemplate;

    public java.util.concurrent.CompletableFuture<org.springframework.kafka.support.SendResult<String, DocumentIngestEvent>> publishIngest(DocumentIngestEvent event) {
        var future = kafkaTemplate.send("document.ingest", event.documentId(), event);
        log.info("Published ingest event documentId={} requestId={}", event.documentId(), event.requestId());
        return future;
    }
}
