package com.retail.copilot.dto.inventory;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventoryItemDto {
    private final String sku;
    private final String productName;
    private final String category;
    private final int currentStock;
    private final int threshold;
    private final double urgencyScore;
    private final String status;
}
