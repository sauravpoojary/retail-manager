package com.retail.copilot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * Standard response envelope for all API responses.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final String status;
    private final T data;
    private final ApiError error;
    private final OffsetDateTime timestamp;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .data(data)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(ApiError error) {
        return ApiResponse.<T>builder()
                .status("error")
                .error(error)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
