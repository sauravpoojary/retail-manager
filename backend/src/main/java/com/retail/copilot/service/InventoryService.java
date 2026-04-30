package com.retail.copilot.service;

import com.retail.copilot.dao.InventoryDao;
import com.retail.copilot.dto.inventory.InventoryItemDto;
import com.retail.copilot.dto.inventory.InventoryResponse;
import com.retail.copilot.exception.StoreNotFoundException;
import com.retail.copilot.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final StoreRepository storeRepository;
    private final InventoryDao inventoryDao;

    @Transactional(readOnly = true)
    public InventoryResponse getLowStockInventory(String storeCode) {
        UUID storeId = storeRepository.findByStoreCode(storeCode)
                .orElseThrow(() -> new StoreNotFoundException(storeCode))
                .getId();

        List<Map<String, Object>> rows = inventoryDao.getLowStockItems(storeId);
        OffsetDateTime asOf = inventoryDao.getLastUpdatedAt(storeId);

        List<InventoryItemDto> items = rows.stream()
                .map(this::toDto)
                .toList();

        return InventoryResponse.builder()
                .storeCode(storeCode)
                .asOf(asOf)
                .items(items)
                .build();
    }

    private InventoryItemDto toDto(Map<String, Object> row) {
        int stock     = ((Number) row.get("current_stock")).intValue();
        int threshold = ((Number) row.get("threshold")).intValue();
        double urgency = ((Number) row.get("urgency_score")).doubleValue();
        String status  = (String) row.get("status");

        return InventoryItemDto.builder()
                .sku((String) row.get("sku"))
                .productName((String) row.get("product_name"))
                .category((String) row.get("category"))
                .currentStock(stock)
                .threshold(threshold)
                .urgencyScore(urgency)
                .status(status)
                .build();
    }
}
