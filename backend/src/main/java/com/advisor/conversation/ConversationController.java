package com.advisor.conversation;

import com.advisor.conversation.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    private String getUserId(Authentication auth) {
        return (String) auth.getCredentials();
    }

    @PostMapping
    public ResponseEntity<ConversationDTO> create(@RequestBody Map<String, String> body,
                                                   Authentication auth) {
        return ResponseEntity.ok(conversationService.create(getUserId(auth), body.get("title")));
    }

    @GetMapping
    public ResponseEntity<Page<ConversationDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        return ResponseEntity.ok(conversationService.list(getUserId(auth), page, size));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageDTO>> getMessages(
            @PathVariable UUID id,
            @RequestParam(required = false) String before,
            @RequestParam(defaultValue = "50") int size,
            Authentication auth) {
        return ResponseEntity.ok(conversationService.getMessages(id.toString(), before, size));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<MessageDTO> sendMessage(
            @PathVariable UUID id,
            @RequestBody SendMessageRequest request,
            Authentication auth,
            @RequestHeader(value = "X-Request-Id", defaultValue = "") String requestId) {
        return ResponseEntity.ok(
                conversationService.sendMessage(id.toString(), getUserId(auth), request, requestId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication auth) {
        conversationService.delete(id.toString(), getUserId(auth));
        return ResponseEntity.noContent().build();
    }
}
