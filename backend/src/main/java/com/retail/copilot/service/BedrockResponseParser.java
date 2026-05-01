package com.retail.copilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.copilot.dto.insight.InsightReasonDto;
import com.retail.copilot.dto.recommendation.RecommendationItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Parses Claude's JSON responses into typed DTOs.
 * All methods are defensive — they never throw; they return empty lists on parse failure
 * so the calling service can fall back gracefully.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BedrockResponseParser {

    private final ObjectMapper objectMapper;

    /**
     * Parses Claude's insight response JSON into a list of InsightReasonDto.
     * Expected shape: { "reasons": [ { "rank": 1, "description": "...", "category": "..." } ] }
     */
    public List<InsightReasonDto> parseInsights(String json) {
        List<InsightReasonDto> reasons = new ArrayList<>();
        try {
            String cleaned = extractJson(json);
            JsonNode root = objectMapper.readTree(cleaned);
            JsonNode reasonsNode = root.path("reasons");

            if (!reasonsNode.isArray()) {
                log.warn("Bedrock insights response missing 'reasons' array. Raw: {}", json);
                return reasons;
            }

            int rank = 1;
            for (JsonNode node : reasonsNode) {
                String description = node.path("description").asText("").trim();
                String category    = node.path("category").asText("sales").trim().toLowerCase();

                if (!description.isBlank()) {
                    reasons.add(InsightReasonDto.builder()
                            .rank(node.has("rank") ? node.path("rank").asInt(rank) : rank)
                            .description(description)
                            .category(sanitizeInsightCategory(category))
                            .build());
                    rank++;
                }

                if (reasons.size() >= 5) break; // enforce max 5
            }
        } catch (Exception e) {
            log.error("Failed to parse Bedrock insights response: {}", e.getMessage());
        }
        return reasons;
    }

    /**
     * Parses Claude's recommendations response JSON.
     * Expected shape: { "recommendations": [ { "priority": "...", "category": "...", ... } ] }
     */
    public List<RecommendationItemDto> parseRecommendations(String json) {
        List<RecommendationItemDto> items = new ArrayList<>();
        try {
            String cleaned = extractJson(json);
            JsonNode root = objectMapper.readTree(cleaned);
            JsonNode recsNode = root.path("recommendations");

            if (!recsNode.isArray()) {
                log.warn("Bedrock recommendations response missing 'recommendations' array. Raw: {}", json);
                return items;
            }

            for (JsonNode node : recsNode) {
                String description = node.path("description").asText("").trim();
                String priority    = node.path("priority").asText("medium").trim().toLowerCase();
                String category    = node.path("category").asText("restocking").trim().toLowerCase();
                String actionLabel = node.path("actionLabel").asText("Take Action").trim();

                if (!description.isBlank()) {
                    items.add(RecommendationItemDto.builder()
                            .id(UUID.randomUUID())
                            .priority(sanitizePriority(priority))
                            .category(sanitizeRecommendationCategory(category))
                            .description(description)
                            .actionLabel(actionLabel)
                            .build());
                }

                if (items.size() >= 3) break; // enforce max 3
            }
        } catch (Exception e) {
            log.error("Failed to parse Bedrock recommendations response: {}", e.getMessage());
        }
        return items;
    }

    /**
     * Extracts plain text from Claude's copilot response.
     * Claude returns plain text for conversational queries — no JSON parsing needed.
     */
    public String parseCopilotReply(String raw) {
        if (raw == null || raw.isBlank()) return "";
        return raw.trim();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Claude sometimes wraps JSON in markdown code fences (```json ... ```).
     * Strip them if present.
     */
    private String extractJson(String raw) {
        if (raw == null) return "{}";
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf('\n');
            int end   = trimmed.lastIndexOf("```");
            if (start >= 0 && end > start) {
                return trimmed.substring(start + 1, end).trim();
            }
        }
        // Find first { and last } to extract JSON object
        int first = trimmed.indexOf('{');
        int last  = trimmed.lastIndexOf('}');
        if (first >= 0 && last > first) {
            return trimmed.substring(first, last + 1);
        }
        return trimmed;
    }

    private String sanitizeInsightCategory(String raw) {
        return switch (raw) {
            case "footfall", "inventory", "staffing", "external" -> raw;
            default -> "sales";
        };
    }

    private String sanitizePriority(String raw) {
        return switch (raw) {
            case "high", "low" -> raw;
            default -> "medium";
        };
    }

    private String sanitizeRecommendationCategory(String raw) {
        return switch (raw) {
            case "staffing", "promotion" -> raw;
            default -> "restocking";
        };
    }
}
