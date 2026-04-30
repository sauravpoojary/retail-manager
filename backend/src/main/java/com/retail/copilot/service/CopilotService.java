package com.retail.copilot.service;

import com.retail.copilot.dao.CopilotDao;
import com.retail.copilot.dto.copilot.*;
import com.retail.copilot.exception.SessionNotFoundException;
import com.retail.copilot.exception.StoreNotFoundException;
import com.retail.copilot.model.CopilotMessage;
import com.retail.copilot.model.CopilotSession;
import com.retail.copilot.model.MessageRole;
import com.retail.copilot.model.Store;
import com.retail.copilot.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CopilotService {

    private final StoreRepository storeRepository;
    private final CopilotDao copilotDao;
    private final PromptServiceClient promptServiceClient;

    @Transactional
    public CreateSessionResponse createSession(String storeCode) {
        Store store = storeRepository.findByStoreCode(storeCode)
                .orElseThrow(() -> new StoreNotFoundException(storeCode));

        CopilotSession session = CopilotSession.builder()
                .store(store)
                .build();

        CopilotSession saved = copilotDao.createSession(session);

        return CreateSessionResponse.builder()
                .sessionId(saved.getId())
                .storeCode(storeCode)
                .startedAt(saved.getStartedAt())
                .build();
    }

    @Transactional
    public CopilotQueryResponse query(CopilotQueryRequest request) {
        // Validate query is not whitespace-only
        if (request.getQuery().isBlank()) {
            throw new IllegalArgumentException("Query must contain non-whitespace characters");
        }

        // Validate session exists
        CopilotSession session = copilotDao.findSession(request.getSessionId())
                .orElseThrow(() -> new SessionNotFoundException(request.getSessionId()));

        // Persist user message
        CopilotMessage userMessage = CopilotMessage.builder()
                .session(session)
                .role(MessageRole.user)
                .content(request.getQuery())
                .isFallback(false)
                .build();
        copilotDao.saveMessage(userMessage);

        // Call prompt service
        String responseContent;
        boolean isFallback = false;

        try {
            responseContent = promptServiceClient.fetchCopilotReply(request);
        } catch (Exception ex) {
            log.warn("Prompt service unavailable for copilot query, returning fallback. Cause: {}", ex.getMessage());
            responseContent = "I'm sorry, the AI service is temporarily unavailable. " +
                    "Please try again in a few moments. In the meantime, you can review " +
                    "your KPI dashboard for the latest store metrics.";
            isFallback = true;
        }

        // Persist assistant message
        CopilotMessage assistantMessage = CopilotMessage.builder()
                .session(session)
                .role(MessageRole.assistant)
                .content(responseContent)
                .isFallback(isFallback)
                .build();
        CopilotMessage saved = copilotDao.saveMessage(assistantMessage);

        // Update session activity
        copilotDao.touchSession(session);

        return CopilotQueryResponse.builder()
                .id(saved.getId())
                .role("assistant")
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt())
                .isFallback(saved.isFallback())
                .build();
    }
}
