package com.ntnn.market.api;

import com.ntnn.market.domain.MarketAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MarketSignalResponse(
        String symbol,
        String modelType,
        MarketAction status,
        double lastClose,
        Double confidence,
        Double qValue,
        LocalDate startDate,
        LocalDate endDate,
        int iterations,
        int trainedRows,
        LocalDateTime createdAt
) {
}
