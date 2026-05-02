package com.ntnn.market.domain;

public record MarketState(
        TrendBucket shortTrend,
        TrendBucket mediumTrend,
        VolatilityBucket volatility
) {
    public enum TrendBucket {
        DOWN,
        FLAT,
        UP
    }

    public enum VolatilityBucket {
        LOW,
        MEDIUM,
        HIGH
    }
}
