package com.advisor.document;

import com.advisor.document.dto.DocumentDTO;
import com.advisor.proxy.AiProxyService;
import com.advisor.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final AiProxyService aiProxyService;

    public DocumentDTO upload(MultipartFile file, String userId, String requestId) throws IOException {
        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Only PDF files are accepted");
        }
        if (file.getSize() > 25L * 1024 * 1024) {
            throw new IllegalArgumentException("File exceeds 25MB limit");
        }
        var user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        var doc = new Document();
        doc.setUser(user);
        doc.setFilename(file.getOriginalFilename());
        doc.setFileSize(file.getSize());
        doc.setStatus("PROCESSING");
        doc = documentRepository.save(doc);

        try {
            aiProxyService.ingestDocument(doc.getId().toString(), userId,
                    file.getOriginalFilename(), file.getBytes(), requestId);
            doc.setStatus("READY");
        } catch (Exception e) {
            log.error("Ingestion failed for document={} requestId={}", doc.getId(), requestId, e);
            doc.setStatus("FAILED");
        }
        doc = documentRepository.save(doc);
        return DocumentDTO.from(doc);
    }

    public Page<DocumentDTO> listDocuments(String userId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
        return documentRepository.findByUserId(UUID.fromString(userId), pageable)
                .map(DocumentDTO::from);
    }

    public void delete(String documentId, String userId, String requestId) {
        var doc = documentRepository.findByIdAndUserId(
                UUID.fromString(documentId), UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        aiProxyService.deleteDocument(documentId, requestId);
        documentRepository.delete(doc);
    }
}
