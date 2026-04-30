package com.retail.copilot.controller;

import com.retail.copilot.dto.ApiResponse;
import com.retail.copilot.dto.insight.StoreContextRequest;
import com.retail.copilot.dto.recommendation.RecommendationResponse;
import com.retail.copilot.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * POST /api/v1/recommendations
     * Generates 2-3 prioritized corrective action recommendations via the Prompt Service.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RecommendationResponse>> getRecommendations(
            @Valid @RequestBody StoreContextRequest request) {
        RecommendationResponse response = recommendationService.getRecommendations(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
