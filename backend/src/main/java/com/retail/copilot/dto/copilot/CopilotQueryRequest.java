package com.retail.copilot.dto.copilot;

import com.retail.copilot.dto.insight.StoreContextRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class CopilotQueryRequest {

    @NotNull(message = "sessionId is required")
    private UUID sessionId;

    @NotBlank(message = "query is required")
    private String query;

    @NotNull(message = "storeContext is required")
    @Valid
    private StoreContextRequest storeContext;

    @NotNull(message = "conversationHistory is required")
    private List<ConversationMessageDto> conversationHistory;
}
