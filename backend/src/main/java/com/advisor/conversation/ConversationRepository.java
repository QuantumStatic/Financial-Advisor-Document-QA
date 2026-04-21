package com.advisor.conversation;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    Page<Conversation> findByUserId(UUID userId, Pageable pageable);
    Optional<Conversation> findByIdAndUserId(UUID id, UUID userId);
}
