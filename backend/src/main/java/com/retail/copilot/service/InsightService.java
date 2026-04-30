package com.retail.copilot.service;

import com.retail.copilot.dto.insight.InsightReasonDto;
import com.retail.copilot.dto.insight.InsightResponse;
import com.retail.copilot.dto.insight.StoreContextRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Delegates to the Prompt Service (external call) for AI-generated insights.
 * Falls back to static responses when the Prompt Service is unavailable.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InsightService {

    private final PromptServiceClient promptServiceClient;

    public InsightResponse getInsights(StoreContextRequest context) {
        try {
            return promptServiceClient.fetchInsights(context);
        } catch (Exception ex) {
            log.warn("Prompt service unavailable for insights, returning fallback. Cause: {}", ex.getMessage());
            return buildFallback(context.getStoreCode());
        }
    }

    private InsightResponse buildFallback(String storeCode) {
        return InsightResponse.builder()
                .storeCode(storeCode)
                .generatedAt(OffsetDateTime.now())
                .reasons(List.of(
                        InsightReasonDto.builder()
                                .rank(1)
                                .description("AI insights are temporarily unavailable. " +
                                        "Common factors affecting sales include inventory gaps, " +
                                        "staffing levels, and local events.")
                                .category("sales")
                                .build()
                ))
                .isFallback(true)
                .build();
    }
}
