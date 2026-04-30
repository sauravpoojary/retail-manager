package com.retail.copilot.service;

import com.retail.copilot.dto.copilot.CopilotQueryRequest;
import com.retail.copilot.dto.insight.InsightReasonDto;
import com.retail.copilot.dto.insight.InsightResponse;
import com.retail.copilot.dto.insight.StoreContextRequest;
import com.retail.copilot.dto.recommendation.RecommendationItemDto;
import com.retail.copilot.dto.recommendation.RecommendationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP client for the internal Prompt Service.
 * Calls /internal/prompt/* endpoints on the Prompt Service.
 *
 * Replace the stub implementations below with real RestClient calls
 * once the Prompt Service is deployed.
 */
@Slf4j
@Component
public class PromptServiceClient {

    private final RestClient restClient;

    public PromptServiceClient(
            @Value("${prompt-service.base-url:http://localhost:8081}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Calls POST /internal/prompt/insights on the Prompt Service.
     * Currently returns a stub response — replace with real HTTP call.
     */
    public InsightResponse fetchInsights(StoreContextRequest context) {
        log.debug("Calling prompt service for insights: storeCode={}", context.getStoreCode());

        // TODO: replace stub with:
        // return restClient.post()
        //     .uri("/internal/prompt/insights")
        //     .body(Map.of("storeContext", context, "maxReasons", 5))
        //     .retrieve()
        //     .body(InsightResponse.class);

        return InsightResponse.builder()
                .storeCode(context.getStoreCode())
                .generatedAt(OffsetDateTime.now())
                .reasons(List.of(
                        InsightReasonDto.builder().rank(1)
                                .description("Morning foot traffic dropped due to road construction nearby.")
                                .category("external").build(),
                        InsightReasonDto.builder().rank(2)
                                .description("Three high-demand dairy products are critically low.")
                                .category("inventory").build(),
                        InsightReasonDto.builder().rank(3)
                                .description("Checkout wait times increased during lunch rush.")
                                .category("staffing").build()
                ))
                .isFallback(false)
                .build();
    }

    /**
     * Calls POST /internal/prompt/recommendations on the Prompt Service.
     */
    public RecommendationResponse fetchRecommendations(StoreContextRequest context) {
        log.debug("Calling prompt service for recommendations: storeCode={}", context.getStoreCode());

        // TODO: replace stub with real HTTP call

        return RecommendationResponse.builder()
                .storeCode(context.getStoreCode())
                .generatedAt(OffsetDateTime.now())
                .recommendations(List.of(
                        RecommendationItemDto.builder()
                                .id(UUID.randomUUID())
                                .priority("high")
                                .category("restocking")
                                .description("Immediately restock top low-stock items.")
                                .actionLabel("Restock Now")
                                .build(),
                        RecommendationItemDto.builder()
                                .id(UUID.randomUUID())
                                .priority("high")
                                .category("staffing")
                                .description("Reallocate staff to checkout during lunch rush.")
                                .actionLabel("Adjust Schedule")
                                .build(),
                        RecommendationItemDto.builder()
                                .id(UUID.randomUUID())
                                .priority("medium")
                                .category("promotion")
                                .description("Launch a promotion on household essentials to counter competitor pricing.")
                                .actionLabel("Start Promotion")
                                .build()
                ))
                .isFallback(false)
                .build();
    }

    /**
     * Calls POST /internal/prompt/query on the Prompt Service.
     */
    public String fetchCopilotReply(CopilotQueryRequest request) {
        log.debug("Calling prompt service for copilot query: sessionId={}", request.getSessionId());

        // TODO: replace stub with real HTTP call

        return "Based on your store data, here is a summary: sales are trending positively " +
                "with " + request.getStoreContext().getFootfall() + " customers today. " +
                "You have " + request.getStoreContext().getLowStockCount() + " items needing attention.";
    }
}
