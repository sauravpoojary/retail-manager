package com.retail.copilot.exception;

import java.util.UUID;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(UUID sessionId) {
        super("Copilot session " + sessionId + " not found");
    }
}
