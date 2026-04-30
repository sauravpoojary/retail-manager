package com.retail.copilot.controller;

import com.retail.copilot.dto.ApiResponse;
import com.retail.copilot.dto.copilot.*;
import com.retail.copilot.service.CopilotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/copilot")
@RequiredArgsConstructor
public class CopilotController {

    private final CopilotService copilotService;

    /**
     * POST /api/v1/copilot/sessions
     * Creates a new copilot session. Returns sessionId (UUID) for use in subsequent queries.
     */
    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<CreateSessionResponse>> createSession(
            @Valid @RequestBody CreateSessionRequest request) {
        CreateSessionResponse response = copilotService.createSession(request.getStoreCode());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * POST /api/v1/copilot/query
     * Sends a query to the AI copilot. Persists user message and assistant reply.
     * Validates session exists and query is not whitespace-only.
     */
    @PostMapping("/query")
    public ResponseEntity<ApiResponse<CopilotQueryResponse>> query(
            @Valid @RequestBody CopilotQueryRequest request) {
        CopilotQueryResponse response = copilotService.query(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
