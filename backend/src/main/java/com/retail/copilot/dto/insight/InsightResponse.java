package com.retail.copilot.dto.insight;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class InsightResponse {
    private final String storeCode;
    private final OffsetDateTime generatedAt;
    private final List<InsightReasonDto> reasons;
    private final boolean isFallback;
}
