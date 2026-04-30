package com.retail.copilot.dto.copilot;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationMessageDto {
    private UUID id;
    private String role;
    private String content;
    private OffsetDateTime createdAt;
}
