package com.advisor.conversation.dto;

import com.advisor.conversation.Message;
import lombok.Data;
import java.time.Instant;
import java.util.*;

@Data
public class MessageDTO {
    private UUID id;
    private String role;
    private String content;
    private List<String> documentIds;
    private Instant createdAt;

    public static MessageDTO from(Message m) {
        var dto = new MessageDTO();
        dto.setId(m.getId());
        dto.setRole(m.getRole());
        dto.setContent(m.getContent());
        dto.setDocumentIds(m.getDocumentIds());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }
}
