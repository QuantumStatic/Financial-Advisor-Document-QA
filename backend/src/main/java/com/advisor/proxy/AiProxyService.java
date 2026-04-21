package com.advisor.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiProxyService {

    private final WebClient webClient;

    @Value("${advisor.python-service-url}")
    private String pythonServiceUrl;

    public Map ingestDocument(String documentId, String userId,
                               String filename, byte[] fileBytes,
                               String requestId) {
        String encoded = Base64.getEncoder().encodeToString(fileBytes);
        Map<String, Object> body = Map.of(
                "document_id", documentId,
                "user_id", userId,
                "filename", filename,
                "file_bytes", encoded
        );
        log.info("Proxying ingest to Python service documentId={} requestId={}", documentId, requestId);
        return webClient.post()
                .uri(pythonServiceUrl + "/ingest")
                .header("X-Request-Id", requestId)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map chat(Map<String, Object> chatPayload, String requestId) {
        log.info("Proxying chat to Python service requestId={}", requestId);
        return webClient.post()
                .uri(pythonServiceUrl + "/chat")
                .header("X-Request-Id", requestId)
                .bodyValue(chatPayload)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public void deleteDocument(String documentId, String requestId) {
        webClient.delete()
                .uri(pythonServiceUrl + "/document/" + documentId)
                .header("X-Request-Id", requestId)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
