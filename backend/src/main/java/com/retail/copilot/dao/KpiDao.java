package com.retail.copilot.dao;

import com.retail.copilot.model.OrderStatus;
import com.retail.copilot.repository.FootfallEntryRepository;
import com.retail.copilot.repository.OrderRepository;
import com.retail.copilot.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * DAO for KPI computations. All KPIs are derived at runtime from raw tables.
 */
@Component
@RequiredArgsConstructor
public class KpiDao {

    private final OrderRepository orderRepository;
    private final FootfallEntryRepository footfallEntryRepository;
    private final ProductRepository productRepository;

    public BigDecimal getDailySales(UUID storeId, LocalDate date, ZoneId zoneId) {
        OffsetDateTime from = date.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime to   = date.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();
        return orderRepository.sumTotalAmount(storeId, OrderStatus.completed, from, to);
    }

    public long getDailyFootfall(UUID storeId, LocalDate date, ZoneId zoneId) {
        OffsetDateTime from = date.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime to   = date.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();
        return footfallEntryRepository.countByStoreAndPeriod(storeId, from, to);
    }

    public long getLowStockCount(UUID storeId) {
        return productRepository.countLowStockByStore(storeId);
    }
}
