package com.retail.copilot.dto.kpi;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DailySalesDto {
    private final BigDecimal amount;
    private final String currency;
    private final double trendPercent;
    private final String trendDirection;
}
