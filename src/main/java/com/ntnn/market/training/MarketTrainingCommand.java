package com.ntnn.market.training;

import com.ntnn.market.api.MarketTrainingRequest;
import com.ntnn.market.domain.MarketModelType;

import java.time.LocalDate;

public record MarketTrainingCommand(
        String symbol,
        MarketModelType modelType,
        LocalDate startDate,
        LocalDate endDate,
        int iterations,
        boolean refresh
) {
    public static MarketTrainingCommand from(MarketTrainingRequest request) {
        String symbol = request.symbol() == null ? "" : request.symbol().trim().toUpperCase();
        MarketModelType modelType = MarketModelType.from(request.modelType());
        LocalDate endDate = request.endDate() == null ? LocalDate.now() : request.endDate();
        LocalDate startDate = request.startDate() == null ? endDate.minusYears(5) : request.startDate();
        int iterations = request.iterations() == null ? 500 : request.iterations();
        boolean refresh = Boolean.TRUE.equals(request.refresh());

        validate(symbol, startDate, endDate, iterations);
        return new MarketTrainingCommand(symbol, modelType, startDate, endDate, iterations, refresh);
    }

    private static void validate(String symbol, LocalDate startDate, LocalDate endDate, int iterations) {
        if (symbol.isBlank()) {
            throw new IllegalArgumentException("symbol is required");
        }
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }
        if (iterations <= 0) {
            throw new IllegalArgumentException("iterations must be positive");
        }
    }
}
