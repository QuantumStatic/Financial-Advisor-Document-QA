package com.advisor.kafka;

public record DocumentIngestedEvent(
        String documentId,
        String status,
        int chunkCount,
        String errorMessage
) {}
