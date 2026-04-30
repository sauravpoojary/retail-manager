package com.retail.copilot.repository;

import com.retail.copilot.model.CopilotMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CopilotMessageRepository extends JpaRepository<CopilotMessage, UUID> {

    List<CopilotMessage> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
}
