package com.advisor.conversation.dto;

import lombok.Data;
import java.util.List;

@Data
public class SendMessageRequest {
    private String content;
    private List<String> documentIds;
}
