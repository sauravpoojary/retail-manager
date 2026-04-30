package com.retail.copilot.controller;

import com.retail.copilot.dto.ApiResponse;
import com.retail.copilot.dto.insight.InsightResponse;
import com.retail.copilot.dto.insight.StoreContextRequest;
import com.retail.copilot.service.InsightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/insights")
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;

    /**
     * POST /api/v1/insights
     * Triggers AI-driven sales diagnosis via the Prompt Service.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<InsightResponse>> getInsights(
            @Valid @RequestBody StoreContextRequest request) {
        InsightResponse response = insightService.getInsights(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
