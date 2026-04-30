package com.retail.copilot.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DAO for inventory queries. Uses JdbcClient for the joined low-stock query
 * to avoid N+1 and native-query entity mapping complexity.
 */
@Component
@RequiredArgsConstructor
public class InventoryDao {

    private final JdbcClient jdbcClient;

    /**
     * Returns all low-stock products with their inventory data in one query,
     * sorted by urgency score descending.
     */
    public List<Map<String, Object>> getLowStockItems(UUID storeId) {
        return jdbcClient.sql("""
                SELECT
                    p.sku,
                    p.name                  AS product_name,
                    p.category,
                    pi.current_stock,
                    p.low_stock_threshold   AS threshold,
                    CASE WHEN pi.current_stock = 0
                         THEN 999999.0
                         ELSE ROUND(p.low_stock_threshold::numeric / pi.current_stock, 2)
                    END                     AS urgency_score,
                    CASE WHEN pi.current_stock < (p.low_stock_threshold * 0.3)
                         THEN 'critical'
                         ELSE 'low'
                    END                     AS status
                FROM products p
                JOIN product_inventory pi ON pi.product_id = p.id
                WHERE p.store_id  = :storeId
                  AND p.is_active = true
                  AND pi.current_stock < p.low_stock_threshold
                ORDER BY urgency_score DESC
                """)
                .param("storeId", storeId)
                .query()
                .listOfRows();
    }

    /**
     * Returns the most recent updated_at across all low-stock items for a store.
     * PostgreSQL JDBC driver returns TIMESTAMPTZ as java.time.Instant — convert to OffsetDateTime.
     */
    public OffsetDateTime getLastUpdatedAt(UUID storeId) {
        return jdbcClient.sql("""
                SELECT MAX(pi.updated_at)
                FROM product_inventory pi
                JOIN products p ON p.id = pi.product_id
                WHERE p.store_id  = :storeId
                  AND p.is_active = true
                  AND pi.current_stock < p.low_stock_threshold
                """)
                .param("storeId", storeId)
                .query((rs, _rowNum) -> {
                    java.sql.Timestamp ts = rs.getTimestamp(1);
                    if (ts == null) return OffsetDateTime.now(ZoneOffset.UTC);
                    return ts.toInstant().atOffset(ZoneOffset.UTC);
                })
                .optional()
                .orElse(OffsetDateTime.now(ZoneOffset.UTC));
    }
}
