package com.advisor.kafka;

public record DocumentIngestEvent(
        String documentId,
        String userId,
        String filename,
        String filePath,
        String requestId
) {}
