package com.retail.copilot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Generates realistic random daily data (orders + footfall) for the demo store.
 * Runs on every application startup. If today's data already exists, it skips.
 * Also ensures yesterday's data exists so KPI trends are always meaningful.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailySeedService {

    private final JdbcClient jdbcClient;

    private static final String STORE_ID  = "d47e5f6a-0000-0000-0000-000000000001";
    private static final String TIMEZONE  = "America/New_York";

    // Daily sales target range: $8,000 – $18,000
    private static final int MIN_ORDERS = 60;
    private static final int MAX_ORDERS = 140;

    // Footfall range: 200 – 500 customers/day
    private static final int MIN_FOOTFALL = 200;
    private static final int MAX_FOOTFALL = 500;

    // Order amount range: $8 – $120 per order
    private static final double MIN_ORDER_AMOUNT = 8.0;
    private static final double MAX_ORDER_AMOUNT = 120.0;

    // ~5% of orders are refunded
    private static final double REFUND_RATE = 0.05;

    @Transactional
    public void seedTodayIfMissing() {
        ZoneId zone = ZoneId.of(TIMEZONE);
        LocalDate today     = LocalDate.now(zone);
        LocalDate yesterday = today.minusDays(1);

        boolean todayExists     = hasOrdersForDate(today);
        boolean yesterdayExists = hasOrdersForDate(yesterday);

        if (!todayExists) {
            log.info("No orders found for today ({}). Generating daily seed data...", today);
            generateOrdersForDate(today, zone);
            generateFootfallForDate(today, zone);
            log.info("Daily seed complete for {}", today);
        } else {
            log.info("Today's data already exists for {} — skipping seed.", today);
        }

        if (!yesterdayExists) {
            log.info("No orders found for yesterday ({}). Generating seed data for trend calculation...", yesterday);
            generateOrdersForDate(yesterday, zone);
            generateFootfallForDate(yesterday, zone);
            log.info("Yesterday seed complete for {}", yesterday);
        }
    }

    // ── Check ─────────────────────────────────────────────────────────────────

    private boolean hasOrdersForDate(LocalDate date) {
        ZoneId zone = ZoneId.of(TIMEZONE);
        OffsetDateTime from = date.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime to   = date.plusDays(1).atStartOfDay(zone).toOffsetDateTime();

        Integer count = jdbcClient.sql("""
                SELECT COUNT(*) FROM orders
                WHERE store_id = :storeId
                  AND ordered_at >= :from
                  AND ordered_at < :to
                """)
                .param("storeId", UUID.fromString(STORE_ID))
                .param("from", from)
                .param("to", to)
                .query(Integer.class)
                .single();

        return count != null && count > 0;
    }

    // ── Orders ────────────────────────────────────────────────────────────────

    private void generateOrdersForDate(LocalDate date, ZoneId zone) {
        Random rng = new Random();
        int orderCount = MIN_ORDERS + rng.nextInt(MAX_ORDERS - MIN_ORDERS + 1);

        // Store opens at 08:00, closes at 21:00 — spread orders across the day
        // with a lunch peak (11–14) and afternoon peak (16–18)
        List<OffsetDateTime> timestamps = generateOrderTimestamps(date, zone, orderCount, rng);

        String dateStr = date.toString().replace("-", "");

        for (int i = 0; i < orderCount; i++) {
            String orderRef = String.format("ORD-%s-%04d", dateStr, i + 1);
            double amount   = MIN_ORDER_AMOUNT + rng.nextDouble() * (MAX_ORDER_AMOUNT - MIN_ORDER_AMOUNT);
            BigDecimal total = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
            String status   = rng.nextDouble() < REFUND_RATE ? "refunded" : "completed";

            jdbcClient.sql("""
                    INSERT INTO orders (id, store_id, order_ref, status, total_amount, ordered_at)
                    VALUES (:id, :storeId::uuid, :orderRef, :status::order_status, :total, :orderedAt)
                    ON CONFLICT (order_ref) DO NOTHING
                    """)
                    .param("id",        UUID.randomUUID())
                    .param("storeId",   STORE_ID)
                    .param("orderRef",  orderRef)
                    .param("status",    status)
                    .param("total",     total)
                    .param("orderedAt", timestamps.get(i))
                    .update();
        }

        log.debug("Inserted {} orders for {}", orderCount, date);
    }

    /**
     * Generates realistic order timestamps spread across store hours with
     * a lunch peak (11:00–14:00) and afternoon peak (16:00–18:00).
     */
    private List<OffsetDateTime> generateOrderTimestamps(LocalDate date, ZoneId zone,
                                                          int count, Random rng) {
        List<OffsetDateTime> times = new ArrayList<>(count);

        // Weight distribution: morning 20%, lunch peak 35%, afternoon 25%, evening 20%
        int morning   = (int) (count * 0.20); // 08:00–11:00
        int lunch     = (int) (count * 0.35); // 11:00–14:00
        int afternoon = (int) (count * 0.25); // 14:00–17:00
        int evening   = count - morning - lunch - afternoon; // 17:00–21:00

        times.addAll(randomTimestamps(date, zone,  8, 11, morning,   rng));
        times.addAll(randomTimestamps(date, zone, 11, 14, lunch,     rng));
        times.addAll(randomTimestamps(date, zone, 14, 17, afternoon, rng));
        times.addAll(randomTimestamps(date, zone, 17, 21, evening,   rng));

        return times;
    }

    private List<OffsetDateTime> randomTimestamps(LocalDate date, ZoneId zone,
                                                   int startHour, int endHour,
                                                   int count, Random rng) {
        List<OffsetDateTime> times = new ArrayList<>(count);
        int totalMinutes = (endHour - startHour) * 60;
        for (int i = 0; i < count; i++) {
            int minuteOffset = rng.nextInt(totalMinutes);
            LocalTime time = LocalTime.of(startHour, 0).plusMinutes(minuteOffset);
            times.add(date.atTime(time).atZone(zone).toOffsetDateTime());
        }
        return times;
    }

    // ── Footfall ──────────────────────────────────────────────────────────────

    private void generateFootfallForDate(LocalDate date, ZoneId zone) {
        Random rng = new Random();
        int footfallCount = MIN_FOOTFALL + rng.nextInt(MAX_FOOTFALL - MIN_FOOTFALL + 1);

        // Footfall spread: slightly earlier than orders (people browse before buying)
        int morning   = (int) (footfallCount * 0.25); // 08:00–11:00
        int lunch     = (int) (footfallCount * 0.30); // 11:00–14:00
        int afternoon = (int) (footfallCount * 0.25); // 14:00–17:00
        int evening   = footfallCount - morning - lunch - afternoon;

        List<OffsetDateTime> timestamps = new ArrayList<>(footfallCount);
        timestamps.addAll(randomTimestamps(date, zone,  8, 11, morning,   rng));
        timestamps.addAll(randomTimestamps(date, zone, 11, 14, lunch,     rng));
        timestamps.addAll(randomTimestamps(date, zone, 14, 17, afternoon, rng));
        timestamps.addAll(randomTimestamps(date, zone, 17, 21, evening,   rng));

        for (OffsetDateTime ts : timestamps) {
            jdbcClient.sql("""
                    INSERT INTO footfall_entries (id, store_id, entered_at)
                    VALUES (:id, :storeId::uuid, :enteredAt)
                    """)
                    .param("id",        UUID.randomUUID())
                    .param("storeId",   STORE_ID)
                    .param("enteredAt", ts)
                    .update();
        }

        log.debug("Inserted {} footfall entries for {}", footfallCount, date);
    }
}
