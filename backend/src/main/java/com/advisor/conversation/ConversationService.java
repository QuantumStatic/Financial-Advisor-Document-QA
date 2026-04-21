package com.advisor.conversation;

import com.advisor.conversation.dto.*;
import com.advisor.proxy.AiProxyService;
import com.advisor.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AiProxyService aiProxyService;

    public ConversationDTO create(String userId, String title) {
        var user = userRepository.findById(UUID.fromString(userId)).orElseThrow();
        var conv = new Conversation();
        conv.setUser(user);
        conv.setTitle(title);
        return ConversationDTO.from(conversationRepository.save(conv));
    }

    public Page<ConversationDTO> list(String userId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        return conversationRepository.findByUserId(UUID.fromString(userId), pageable)
                .map(ConversationDTO::from);
    }

    public List<MessageDTO> getMessages(String conversationId, String beforeId, int size) {
        UUID convId = UUID.fromString(conversationId);
        List<Message> messages;
        if (beforeId == null) {
            messages = messageRepository.findTop50ByConversationIdOrderByCreatedAtDesc(convId);
        } else {
            messages = messageRepository
                .findByConversationIdAndIdLessThanOrderByCreatedAtDesc(
                    convId, UUID.fromString(beforeId), PageRequest.of(0, size));
        }
        Collections.reverse(messages);
        return messages.stream().map(MessageDTO::from).collect(Collectors.toList());
    }

    @Transactional
    public MessageDTO sendMessage(String conversationId, String userId,
                                   SendMessageRequest request, String requestId) {
        var conv = conversationRepository.findByIdAndUserId(
                UUID.fromString(conversationId), UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        var userMsg = new Message();
        userMsg.setConversation(conv);
        userMsg.setRole("USER");
        userMsg.setContent(request.getContent());
        userMsg.setDocumentIds(request.getDocumentIds());
        messageRepository.save(userMsg);

        List<Message> history = messageRepository.findByConversationIdOrderByCreatedAtAsc(conv.getId());
        List<Map<String, String>> historyPayload = history.stream()
                .map(m -> Map.of("role", m.getRole().toLowerCase(), "content", m.getContent()))
                .collect(Collectors.toList());

        Map<String, Object> chatPayload = Map.of(
                "message", request.getContent(),
                "user_id", userId,
                "conversation_history", historyPayload,
                "document_ids", request.getDocumentIds() != null ? request.getDocumentIds() : List.of()
        );

        Map aiResponse = aiProxyService.chat(chatPayload, requestId);

        var assistantMsg = new Message();
        assistantMsg.setConversation(conv);
        assistantMsg.setRole("ASSISTANT");
        assistantMsg.setContent((String) aiResponse.get("answer"));
        assistantMsg.setDocumentIds(request.getDocumentIds());
        messageRepository.save(assistantMsg);

        conv.setUpdatedAt(Instant.now());
        conversationRepository.save(conv);

        return MessageDTO.from(assistantMsg);
    }

    public void delete(String conversationId, String userId) {
        var conv = conversationRepository.findByIdAndUserId(
                UUID.fromString(conversationId), UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        conversationRepository.delete(conv);
    }
}
