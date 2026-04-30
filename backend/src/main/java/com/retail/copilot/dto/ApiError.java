package com.retail.copilot.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiError {
    private final String code;
    private final String message;
    private final boolean fallback;
}
