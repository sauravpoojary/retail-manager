package com.retail.copilot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.copilot.dto.copilot.CopilotQueryRequest;
import com.retail.copilot.dto.copilot.ConversationMessageDto;
import com.retail.copilot.dto.insight.InsightReasonDto;
import com.retail.copilot.dto.insight.InsightResponse;
import com.retail.copilot.dto.insight.StoreContextRequest;
import com.retail.copilot.dto.recommendation.RecommendationItemDto;
import com.retail.copilot.dto.recommendation.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Calls Amazon Bedrock (Claude 3 Haiku) directly for AI-powered insights,
 * recommendations, and copilot responses.
 *
 * Uses the Bedrock InvokeModel API with the Claude Messages API format.
 * Structured JSON prompts ensure reliable response parsing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PromptServiceClient {

    private final BedrockRuntimeClient bedrockClient;
    private final BedrockResponseParser responseParser;
    private final ObjectMapper objectMapper;

    // Claude Sonnet 4.6 — active model, no use case form required
    @Value("${bedrock.model-id:us.anthropic.claude-sonnet-4-6}")
    private String modelId;

    @Value("${bedrock.max-tokens:1024}")
    private int maxTokens;

    @Value("${bedrock.temperature:0.7}")
    private double temperature;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Calls Bedrock to generate sales insight reasons.
     * Returns a structured InsightResponse with up to 5 ranked reasons.
     */
    public InsightResponse fetchInsights(StoreContextRequest ctx) {
        log.debug("Calling Bedrock for insights: storeCode={}", ctx.getStoreCode());

        String userPrompt = PromptTemplates.buildInsightsPrompt(
                ctx.getStoreCode(),
                ctx.getDate() != null ? ctx.getDate().toString() : "today",
                ctx.getDailySales() != null ? ctx.getDailySales().doubleValue() : 0,
                ctx.getSalesTrend() != null ? ctx.getSalesTrend() : 0,
                ctx.getFootfall() != null ? ctx.getFootfall() : 0,
                ctx.getFootfallTrend() != null ? ctx.getFootfallTrend() : 0,
                ctx.getLowStockCount() != null ? ctx.getLowStockCount() : 0,
                ctx.getTopLowStockItems() != null ? String.join(", ", ctx.getTopLowStockItems()) : "none"
        );

        String rawResponse = invokeModel(PromptTemplates.INSIGHTS_SYSTEM, userPrompt);
        List<InsightReasonDto> reasons = responseParser.parseInsights(rawResponse);

        if (reasons.isEmpty()) {
            throw new RuntimeException("Bedrock returned unparseable insights response");
        }

        return InsightResponse.builder()
                .storeCode(ctx.getStoreCode())
                .generatedAt(OffsetDateTime.now())
                .reasons(reasons)
                .isFallback(false)
                .build();
    }

    /**
     * Calls Bedrock to generate corrective action recommendations.
     * Returns 2–3 prioritized recommendations.
     */
    public RecommendationResponse fetchRecommendations(StoreContextRequest ctx) {
        log.debug("Calling Bedrock for recommendations: storeCode={}", ctx.getStoreCode());

        String userPrompt = PromptTemplates.buildRecommendationsPrompt(
                ctx.getStoreCode(),
                ctx.getDate() != null ? ctx.getDate().toString() : "today",
                ctx.getDailySales() != null ? ctx.getDailySales().doubleValue() : 0,
                ctx.getSalesTrend() != null ? ctx.getSalesTrend() : 0,
                ctx.getFootfall() != null ? ctx.getFootfall() : 0,
                ctx.getFootfallTrend() != null ? ctx.getFootfallTrend() : 0,
                ctx.getLowStockCount() != null ? ctx.getLowStockCount() : 0,
                ctx.getTopLowStockItems() != null ? String.join(", ", ctx.getTopLowStockItems()) : "none"
        );

        String rawResponse = invokeModel(PromptTemplates.RECOMMENDATIONS_SYSTEM, userPrompt);
        List<RecommendationItemDto> recommendations = responseParser.parseRecommendations(rawResponse);

        if (recommendations.isEmpty()) {
            throw new RuntimeException("Bedrock returned unparseable recommendations response");
        }

        // Enforce 2–3 items
        if (recommendations.size() > 3) {
            recommendations = recommendations.subList(0, 3);
        }

        return RecommendationResponse.builder()
                .storeCode(ctx.getStoreCode())
                .generatedAt(OffsetDateTime.now())
                .recommendations(recommendations)
                .isFallback(false)
                .build();
    }

    /**
     * Calls Bedrock for a conversational copilot reply.
     * Includes conversation history for context continuity.
     */
    public String fetchCopilotReply(CopilotQueryRequest request) {
        log.debug("Calling Bedrock for copilot query: sessionId={}", request.getSessionId());

        StoreContextRequest ctx = request.getStoreContext();

        // Build a readable conversation history string (last 6 messages to stay within token limits)
        String history = buildConversationHistory(request.getConversationHistory());

        String userPrompt = PromptTemplates.buildCopilotPrompt(
                ctx.getStoreCode(),
                ctx.getDate() != null ? ctx.getDate().toString() : "today",
                ctx.getDailySales() != null ? ctx.getDailySales().doubleValue() : 0,
                ctx.getSalesTrend() != null ? ctx.getSalesTrend() : 0,
                ctx.getFootfall() != null ? ctx.getFootfall() : 0,
                ctx.getFootfallTrend() != null ? ctx.getFootfallTrend() : 0,
                ctx.getLowStockCount() != null ? ctx.getLowStockCount() : 0,
                ctx.getTopLowStockItems() != null ? String.join(", ", ctx.getTopLowStockItems()) : "none",
                history,
                request.getQuery()
        );

        String rawResponse = invokeModel(PromptTemplates.COPILOT_SYSTEM, userPrompt);
        return responseParser.parseCopilotReply(rawResponse);
    }

    // ── Bedrock invocation ────────────────────────────────────────────────────

    /**
     * Invokes Claude via the Bedrock InvokeModel API using the Messages API format.
     * Returns the raw text content from Claude's response.
     */
    private String invokeModel(String systemPrompt, String userMessage) {
        try {
            // Build the Claude Messages API request body
            Map<String, Object> requestBody = Map.of(
                    "anthropic_version", "bedrock-2023-05-31",
                    "max_tokens", maxTokens,
                    "temperature", temperature,
                    "system", systemPrompt.trim(),
                    "messages", List.of(
                            Map.of("role", "user", "content", userMessage.trim())
                    )
            );

            String requestJson = objectMapper.writeValueAsString(requestBody);
            log.debug("Bedrock request to model {}: {}", modelId, requestJson);

            InvokeModelRequest invokeRequest = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(requestJson))
                    .build();

            InvokeModelResponse response = bedrockClient.invokeModel(invokeRequest);
            String responseJson = response.body().asUtf8String();
            log.debug("Bedrock raw response: {}", responseJson);

            // Parse Claude's response envelope to extract the text content
            return extractTextContent(responseJson);

        } catch (Exception e) {
            log.error("Bedrock invocation failed for model {}: {}", modelId, e.getMessage());
            throw new RuntimeException("Bedrock invocation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the text content from Claude's response envelope.
     * Claude response shape:
     * {
     *   "content": [ { "type": "text", "text": "..." } ],
     *   "stop_reason": "end_turn",
     *   ...
     * }
     */
    private String extractTextContent(String responseJson) throws Exception {
        var root = objectMapper.readTree(responseJson);
        var contentArray = root.path("content");

        if (contentArray.isArray()) {
            for (var item : contentArray) {
                if ("text".equals(item.path("type").asText())) {
                    return item.path("text").asText("");
                }
            }
        }

        // Fallback: return the whole response body if structure is unexpected
        log.warn("Unexpected Bedrock response structure: {}", responseJson);
        return responseJson;
    }

    /**
     * Formats the last N conversation messages into a readable string for the prompt.
     */
    private String buildConversationHistory(List<ConversationMessageDto> history) {
        if (history == null || history.isEmpty()) return "";

        int start = Math.max(0, history.size() - 6); // last 6 messages
        return history.subList(start, history.size()).stream()
                .map(msg -> {
                    String role = "user".equals(msg.getRole()) ? "Manager" : "Copilot";
                    return role + ": " + msg.getContent();
                })
                .collect(Collectors.joining("\n"));
    }
}
