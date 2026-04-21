package com.advisor.conversation;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByConversationIdAndIdLessThanOrderByCreatedAtDesc(
        UUID conversationId, UUID beforeId, Pageable pageable);
    List<Message> findTop50ByConversationIdOrderByCreatedAtDesc(UUID conversationId);
    List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}
