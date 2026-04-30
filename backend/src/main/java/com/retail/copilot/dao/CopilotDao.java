package com.retail.copilot.dao;

import com.retail.copilot.model.CopilotMessage;
import com.retail.copilot.model.CopilotSession;
import com.retail.copilot.repository.CopilotMessageRepository;
import com.retail.copilot.repository.CopilotSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * DAO for copilot session and message persistence.
 */
@Component
@RequiredArgsConstructor
public class CopilotDao {

    private final CopilotSessionRepository sessionRepository;
    private final CopilotMessageRepository messageRepository;

    public CopilotSession createSession(CopilotSession session) {
        return sessionRepository.save(session);
    }

    public Optional<CopilotSession> findSession(UUID sessionId) {
        return sessionRepository.findById(sessionId);
    }

    public void touchSession(CopilotSession session) {
        session.setLastActiveAt(OffsetDateTime.now());
        sessionRepository.save(session);
    }

    public CopilotMessage saveMessage(CopilotMessage message) {
        return messageRepository.save(message);
    }

    public List<CopilotMessage> getMessages(UUID sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }
}
