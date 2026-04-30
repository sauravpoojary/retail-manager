package com.retail.copilot.controller;

import com.retail.copilot.dto.ApiResponse;
import com.retail.copilot.dto.inventory.InventoryResponse;
import com.retail.copilot.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * GET /api/v1/inventory?storeCode=STORE-042
     * Returns low-stock products sorted by urgency score descending.
     * Returns empty items list when all stock levels are adequate.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventory(
            @RequestParam String storeCode) {
        InventoryResponse response = inventoryService.getLowStockInventory(storeCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
