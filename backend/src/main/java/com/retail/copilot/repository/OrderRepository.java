package com.retail.copilot.repository;

import com.retail.copilot.model.Order;
import com.retail.copilot.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.store.id = :storeId
          AND o.status = :status
          AND o.orderedAt >= :from
          AND o.orderedAt < :to
        """)
    BigDecimal sumTotalAmount(
            @Param("storeId") UUID storeId,
            @Param("status") OrderStatus status,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );
}
