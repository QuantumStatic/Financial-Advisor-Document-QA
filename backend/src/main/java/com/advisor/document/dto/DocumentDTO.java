package com.advisor.document.dto;

import com.advisor.document.Document;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class DocumentDTO {
    private UUID id;
    private String filename;
    private Long fileSize;
    private String status;
    private Instant uploadedAt;

    public static DocumentDTO from(Document doc) {
        var dto = new DocumentDTO();
        dto.setId(doc.getId());
        dto.setFilename(doc.getFilename());
        dto.setFileSize(doc.getFileSize());
        dto.setStatus(doc.getStatus());
        dto.setUploadedAt(doc.getUploadedAt());
        return dto;
    }
}
