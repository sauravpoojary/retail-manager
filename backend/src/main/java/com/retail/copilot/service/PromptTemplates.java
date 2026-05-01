package com.retail.copilot.service;

/**
 * Prompt templates for each AI feature.
 * All prompts instruct Claude to return strict JSON so the response parser
 * can reliably extract structured data.
 */
public final class PromptTemplates {

    private PromptTemplates() {}

    // ── System prompts ────────────────────────────────────────────────────────

    public static final String INSIGHTS_SYSTEM = """
            You are a retail analytics expert. Analyze store performance data and identify
            the top reasons behind sales fluctuations. Always respond with valid JSON only —
            no markdown, no explanation outside the JSON structure.
            """;

    public static final String RECOMMENDATIONS_SYSTEM = """
            You are a retail operations advisor. Generate prioritized corrective action
            recommendations based on store performance data. Always respond with valid JSON
            only — no markdown, no explanation outside the JSON structure.
            """;

    public static final String COPILOT_SYSTEM = """
            You are a helpful retail store copilot assistant. Answer the store manager's
            question concisely and actionably using the provided store context.
            Keep responses under 150 words. Be direct and practical.
            """;

    // ── User prompt builders ──────────────────────────────────────────────────

    /**
     * Builds the insights prompt. Instructs Claude to return JSON matching InsightData shape.
     */
    public static String buildInsightsPrompt(
            String storeCode, String date,
            double dailySales, double salesTrend,
            long footfall, double footfallTrend,
            int lowStockCount, String topLowStockItems) {

        return """
                Store performance data for %s on %s:
                - Daily sales: $%.2f (%.1f%% vs yesterday)
                - Customer footfall: %d (%.1f%% vs yesterday)
                - Low stock alerts: %d items
                - Top low-stock products: %s

                Identify the top reasons for the current sales performance.
                Return ONLY this JSON structure (no other text):
                {
                  "reasons": [
                    { "rank": 1, "description": "...", "category": "sales|footfall|inventory|staffing|external" },
                    { "rank": 2, "description": "...", "category": "..." }
                  ]
                }
                Return between 3 and 5 reasons. Each description must be one concise sentence.
                """.formatted(storeCode, date, dailySales, salesTrend,
                footfall, footfallTrend, lowStockCount, topLowStockItems);
    }

    /**
     * Builds the recommendations prompt. Returns JSON matching RecommendationData shape.
     */
    public static String buildRecommendationsPrompt(
            String storeCode, String date,
            double dailySales, double salesTrend,
            long footfall, double footfallTrend,
            int lowStockCount, String topLowStockItems) {

        return """
                Store performance data for %s on %s:
                - Daily sales: $%.2f (%.1f%% vs yesterday)
                - Customer footfall: %d (%.1f%% vs yesterday)
                - Low stock alerts: %d items
                - Top low-stock products: %s

                Generate 2 to 3 prioritized corrective action recommendations.
                Return ONLY this JSON structure (no other text):
                {
                  "recommendations": [
                    {
                      "priority": "high|medium|low",
                      "category": "promotion|staffing|restocking",
                      "description": "...",
                      "actionLabel": "short CTA (2-3 words)"
                    }
                  ]
                }
                Sort by priority descending. Each description must be one actionable sentence.
                """.formatted(storeCode, date, dailySales, salesTrend,
                footfall, footfallTrend, lowStockCount, topLowStockItems);
    }

    /**
     * Builds the copilot conversation prompt with store context and history.
     */
    public static String buildCopilotPrompt(
            String storeCode, String date,
            double dailySales, double salesTrend,
            long footfall, double footfallTrend,
            int lowStockCount, String topLowStockItems,
            String conversationHistory,
            String userQuery) {

        return """
                Current store context for %s on %s:
                - Daily sales: $%.2f (%.1f%% vs yesterday)
                - Customer footfall: %d (%.1f%% vs yesterday)
                - Low stock alerts: %d items
                - Top low-stock products: %s

                %s

                Store manager asks: %s
                """.formatted(storeCode, date, dailySales, salesTrend,
                footfall, footfallTrend, lowStockCount, topLowStockItems,
                conversationHistory.isBlank() ? "" : "Recent conversation:\n" + conversationHistory,
                userQuery);
    }
}
