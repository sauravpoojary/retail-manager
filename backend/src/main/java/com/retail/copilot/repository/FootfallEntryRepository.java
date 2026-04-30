package com.retail.copilot.repository;

import com.retail.copilot.model.FootfallEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface FootfallEntryRepository extends JpaRepository<FootfallEntry, UUID> {

    @Query("""
        SELECT COUNT(f)
        FROM FootfallEntry f
        WHERE f.store.id = :storeId
          AND f.enteredAt >= :from
          AND f.enteredAt < :to
        """)
    long countByStoreAndPeriod(
            @Param("storeId") UUID storeId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );
}
