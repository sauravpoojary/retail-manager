package com.retail.copilot.service;

import com.retail.copilot.dto.insight.StoreContextRequest;
import com.retail.copilot.dto.recommendation.RecommendationItemDto;
import com.retail.copilot.dto.recommendation.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Delegates to the Prompt Service for AI-generated recommendations.
 * Falls back to static responses when unavailable.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final PromptServiceClient promptServiceClient;

    public RecommendationResponse getRecommendations(StoreContextRequest context) {
        try {
            return promptServiceClient.fetchRecommendations(context);
        } catch (Exception ex) {
            log.warn("Prompt service unavailable for recommendations, returning fallback. Cause: {}", ex.getMessage());
            return buildFallback(context.getStoreCode());
        }
    }

    private RecommendationResponse buildFallback(String storeCode) {
        return RecommendationResponse.builder()
                .storeCode(storeCode)
                .generatedAt(OffsetDateTime.now())
                .recommendations(List.of(
                        RecommendationItemDto.builder()
                                .id(UUID.randomUUID())
                                .priority("medium")
                                .category("restocking")
                                .description("Review and restock any items below threshold levels.")
                                .actionLabel("Check Inventory")
                                .build(),
                        RecommendationItemDto.builder()
                                .id(UUID.randomUUID())
                                .priority("medium")
                                .category("staffing")
                                .description("Ensure adequate checkout coverage during peak hours (11 AM–2 PM).")
                                .actionLabel("Review Schedule")
                                .build()
                ))
                .isFallback(true)
                .build();
    }
}
