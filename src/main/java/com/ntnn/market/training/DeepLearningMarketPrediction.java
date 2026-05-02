package com.ntnn.market.training;

import com.ntnn.market.domain.MarketAction;

public record DeepLearningMarketPrediction(
        String symbol,
        MarketAction action,
        double confidence,
        double buyProbability,
        double holdProbability,
        double sellProbability,
        double lastClose
) {
}
