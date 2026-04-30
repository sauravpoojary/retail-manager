package com.retail.copilot.dto.copilot;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateSessionRequest {

    @NotBlank(message = "storeCode is required")
    private String storeCode;
}
