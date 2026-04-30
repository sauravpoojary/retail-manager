package com.retail.copilot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "footfall_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FootfallEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "entered_at", nullable = false)
    private OffsetDateTime enteredAt;

    @PrePersist
    void prePersist() {
        if (enteredAt == null) enteredAt = OffsetDateTime.now();
    }
}
