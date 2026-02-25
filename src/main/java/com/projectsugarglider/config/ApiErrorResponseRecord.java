package com.projectsugarglider.config;

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