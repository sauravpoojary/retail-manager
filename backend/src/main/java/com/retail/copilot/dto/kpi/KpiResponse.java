package com.retail.copilot.dto.kpi;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KpiResponse {
    private final String storeCode;
    private final LocalDate date;
    private final DailySalesDto dailySales;
    private final FootfallDto footfall;
    private final Long lowStockAlertCount;
    private final OffsetDateTime asOf;
}
