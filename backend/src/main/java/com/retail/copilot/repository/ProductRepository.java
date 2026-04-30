package com.retail.copilot.repository;

import com.retail.copilot.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query(value = """
        SELECT COUNT(p.id)
        FROM products p
        JOIN product_inventory pi ON pi.product_id = p.id
        WHERE p.store_id = :storeId
          AND p.is_active = true
          AND pi.current_stock < p.low_stock_threshold
        """, nativeQuery = true)
    long countLowStockByStore(@Param("storeId") UUID storeId);

    @Query(value = """
        SELECT p.*
        FROM products p
        JOIN product_inventory pi ON pi.product_id = p.id
        WHERE p.store_id = :storeId
          AND p.is_active = true
          AND pi.current_stock < p.low_stock_threshold
        ORDER BY
            CASE WHEN pi.current_stock = 0 THEN 999999.0
                 ELSE p.low_stock_threshold::numeric / pi.current_stock
            END DESC
        """, nativeQuery = true)
    List<Product> findLowStockProductsByStore(@Param("storeId") UUID storeId);
}
