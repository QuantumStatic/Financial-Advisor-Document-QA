package com.advisor.conversation.dto;

import com.advisor.conversation.Conversation;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class ConversationDTO {
    private UUID id;
    private String title;
    private Instant createdAt;
    private Instant updatedAt;

    public static ConversationDTO from(Conversation c) {
        var dto = new ConversationDTO();
        dto.setId(c.getId());
        dto.setTitle(c.getTitle());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    }
}
