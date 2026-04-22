package com.advisor.document;

import com.advisor.document.dto.DocumentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    private String getUserId(Authentication auth) {
        return (String) auth.getCredentials();
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentDTO> upload(@RequestParam("file") MultipartFile file,
                                              Authentication auth,
                                              @RequestHeader(value = "X-Request-Id", defaultValue = "") String requestId) throws IOException {
        return ResponseEntity.ok(documentService.upload(file, getUserId(auth), requestId));
    }

    @GetMapping
    public ResponseEntity<Page<DocumentDTO>> list(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   Authentication auth) {
        return ResponseEntity.ok(documentService.listDocuments(getUserId(auth), page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                        Authentication auth,
                                        @RequestHeader(value = "X-Request-Id", defaultValue = "") String requestId) {
        documentService.delete(id.toString(), getUserId(auth), requestId);
        return ResponseEntity.noContent().build();
    }
}
