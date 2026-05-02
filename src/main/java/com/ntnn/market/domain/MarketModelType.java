package com.ntnn.market.domain;

public enum MarketModelType {
    Q_LEARNING,
    DL4J;

    public static MarketModelType from(String value) {
        if (value != null && value.equalsIgnoreCase("dl4j")) {
            return DL4J;
        }
        return Q_LEARNING;
    }
}
