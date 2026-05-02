package com.ntnn.market.domain;

public record MarketPrediction(
        String symbol,
        MarketState state,
        MarketAction action,
        double qValue,
        double lastClose
) {
}
