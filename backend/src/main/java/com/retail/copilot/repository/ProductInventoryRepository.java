package com.retail.copilot.repository;

import com.retail.copilot.model.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ProductInventoryRepository extends JpaRepository<ProductInventory, UUID> {

    Optional<ProductInventory> findByProductId(UUID productId);

    @Query(value = """
        SELECT MAX(pi.updated_at)
        FROM product_inventory pi
        JOIN products p ON p.id = pi.product_id
        WHERE p.store_id  = :storeId
          AND p.is_active = true
          AND pi.current_stock < p.low_stock_threshold
        """, nativeQuery = true)
    Optional<OffsetDateTime> findMaxUpdatedAtForLowStock(@Param("storeId") UUID storeId);
}
