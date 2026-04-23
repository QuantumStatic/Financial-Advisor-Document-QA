package com.advisor.kafka;

import com.advisor.document.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentIngestedListener {

    private final DocumentRepository documentRepository;

    @KafkaListener(topics = "document.ingested", groupId = "rag-backend")
    @Transactional
    public void onDocumentIngested(DocumentIngestedEvent event) {
        log.info("Received ingested event documentId={} status={}", event.documentId(), event.status());
        documentRepository.findById(java.util.UUID.fromString(event.documentId()))
                .ifPresent(doc -> {
                    doc.setStatus(event.status());
                    documentRepository.save(doc);
                    log.info("Updated document status documentId={} status={}", event.documentId(), event.status());
                });
    }
}
