package com.retail.copilot.dto.recommendation;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class RecommendationItemDto {
    private final UUID id;
    private final String priority;
    private final String category;
    private final String description;
    private final String actionLabel;
}
