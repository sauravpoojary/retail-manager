package com.retail.copilot.dto.insight;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Shared store context used by insights, recommendations, and copilot query endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
public class StoreContextRequest {

    @NotBlank(message = "storeCode is required")
    private String storeCode;

    @NotNull(message = "date is required")
    private LocalDate date;

    @NotNull(message = "dailySales is required")
    private BigDecimal dailySales;

    @NotNull(message = "salesTrend is required")
    private Double salesTrend;

    @NotNull(message = "footfall is required")
    private Long footfall;

    @NotNull(message = "footfallTrend is required")
    private Double footfallTrend;

    @NotNull(message = "lowStockCount is required")
    private Integer lowStockCount;

    @NotNull(message = "topLowStockItems is required")
    private List<String> topLowStockItems;
}
