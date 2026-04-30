package com.retail.copilot.service;

import com.retail.copilot.dao.KpiDao;
import com.retail.copilot.dto.kpi.DailySalesDto;
import com.retail.copilot.dto.kpi.FootfallDto;
import com.retail.copilot.dto.kpi.KpiResponse;
import com.retail.copilot.exception.StoreNotFoundException;
import com.retail.copilot.model.Store;
import com.retail.copilot.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class KpiService {

    private final StoreRepository storeRepository;
    private final KpiDao kpiDao;

    @Transactional(readOnly = true)
    public KpiResponse getKpis(String storeCode) {
        Store store = storeRepository.findByStoreCode(storeCode)
                .orElseThrow(() -> new StoreNotFoundException(storeCode));

        ZoneId zoneId = ZoneId.of(store.getTimezone());
        LocalDate today     = LocalDate.now(zoneId);
        LocalDate yesterday = today.minusDays(1);

        // Sales
        BigDecimal todaySales     = kpiDao.getDailySales(store.getId(), today, zoneId);
        BigDecimal yesterdaySales = kpiDao.getDailySales(store.getId(), yesterday, zoneId);
        DailySalesDto dailySales  = buildDailySales(todaySales, yesterdaySales, store.getCurrency());

        // Footfall
        long todayFootfall     = kpiDao.getDailyFootfall(store.getId(), today, zoneId);
        long yesterdayFootfall = kpiDao.getDailyFootfall(store.getId(), yesterday, zoneId);
        FootfallDto footfall   = buildFootfall(todayFootfall, yesterdayFootfall);

        // Low stock
        long lowStockCount = kpiDao.getLowStockCount(store.getId());

        return KpiResponse.builder()
                .storeCode(storeCode)
                .date(today)
                .dailySales(dailySales)
                .footfall(footfall)
                .lowStockAlertCount(lowStockCount)
                .asOf(OffsetDateTime.now())
                .build();
    }

    private DailySalesDto buildDailySales(BigDecimal today, BigDecimal yesterday, String currency) {
        double trend = computeTrend(today.doubleValue(), yesterday.doubleValue());
        return DailySalesDto.builder()
                .amount(today)
                .currency(currency)
                .trendPercent(round(trend))
                .trendDirection(trendDirection(trend))
                .build();
    }

    private FootfallDto buildFootfall(long today, long yesterday) {
        double trend = computeTrend(today, yesterday);
        return FootfallDto.builder()
                .count(today)
                .trendPercent(round(trend))
                .trendDirection(trendDirection(trend))
                .build();
    }

    private double computeTrend(double today, double yesterday) {
        if (yesterday == 0) return 0.0;
        return ((today - yesterday) / yesterday) * 100.0;
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private String trendDirection(double trend) {
        if (trend > 0) return "up";
        if (trend < 0) return "down";
        return "flat";
    }
}
