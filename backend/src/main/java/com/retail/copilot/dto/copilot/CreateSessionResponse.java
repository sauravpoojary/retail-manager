package com.retail.copilot.dto.copilot;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class CreateSessionResponse {
    private final UUID sessionId;
    private final String storeCode;
    private final OffsetDateTime startedAt;
}
