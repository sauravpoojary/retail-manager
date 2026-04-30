package com.retail.copilot.repository;

import com.retail.copilot.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {
    Optional<Store> findByStoreCode(String storeCode);
}
