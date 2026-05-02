package com.ntnn.market.api;

import java.time.LocalDate;

public record MarketTrainingRequest(
        String symbol,
        String modelType,
        LocalDate startDate,
        LocalDate endDate,
        Integer iterations,
        Boolean refresh
) {
}
