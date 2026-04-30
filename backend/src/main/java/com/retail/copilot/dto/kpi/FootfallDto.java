package com.retail.copilot.dto.kpi;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FootfallDto {
    private final long count;
    private final double trendPercent;
    private final String trendDirection;
}
