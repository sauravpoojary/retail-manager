package com.retail.copilot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "stores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "store_code", nullable = false, unique = true, length = 20)
    private String storeCode;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 50)
    private String timezone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
    }
}
