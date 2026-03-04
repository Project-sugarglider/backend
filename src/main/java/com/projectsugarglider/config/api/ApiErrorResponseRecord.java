package com.projectsugarglider.config.api;

import java.time.Instant;

public record ApiErrorResponseRecord(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String requestId
) {
}