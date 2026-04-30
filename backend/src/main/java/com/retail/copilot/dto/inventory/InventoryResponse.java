package com.retail.copilot.dto.inventory;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class InventoryResponse {
    private final String storeCode;
    private final OffsetDateTime asOf;
    private final List<InventoryItemDto> items;
}
