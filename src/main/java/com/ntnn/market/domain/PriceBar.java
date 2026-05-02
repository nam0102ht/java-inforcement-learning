package com.ntnn.market.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public record PriceBar(LocalDate date, double close) {
    public static PriceBar fromEpochSecond(long epochSecond, double close) {
        return new PriceBar(Instant.ofEpochSecond(epochSecond).atZone(ZoneOffset.UTC).toLocalDate(), close);
    }
}
