package com.ntnn.market.api;

import java.time.LocalDateTime;

public record ApiErrorResponse(
        String error,
        String message,
        LocalDateTime timestamp
) {
}
