package com.retail.copilot.dto.insight;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InsightReasonDto {
    private final int rank;
    private final String description;
    private final String category;
}
