package com.retail.copilot.dto.recommendation;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class RecommendationResponse {
    private final String storeCode;
    private final OffsetDateTime generatedAt;
    private final List<RecommendationItemDto> recommendations;
    private final boolean isFallback;
}
