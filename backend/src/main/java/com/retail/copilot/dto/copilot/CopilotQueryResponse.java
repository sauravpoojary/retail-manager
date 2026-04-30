package com.retail.copilot.dto.copilot;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class CopilotQueryResponse {
    private final UUID id;
    private final String role;
    private final String content;
    private final OffsetDateTime createdAt;
    private final boolean isFallback;
}
