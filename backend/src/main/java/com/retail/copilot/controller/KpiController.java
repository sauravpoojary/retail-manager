package com.retail.copilot.controller;

import com.retail.copilot.dto.ApiResponse;
import com.retail.copilot.dto.kpi.KpiResponse;
import com.retail.copilot.service.KpiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/kpis")
@RequiredArgsConstructor
public class KpiController {

    private final KpiService kpiService;

    /**
     * GET /api/v1/kpis?storeCode=STORE-042
     * Returns runtime-computed KPIs: daily sales, footfall, low-stock count.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<KpiResponse>> getKpis(
            @RequestParam String storeCode) {
        KpiResponse kpis = kpiService.getKpis(storeCode);
        return ResponseEntity.ok(ApiResponse.success(kpis));
    }
}
