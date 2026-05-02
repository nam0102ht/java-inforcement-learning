package com.ntnn.market.yahoo;

import com.ntnn.market.domain.PriceBar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class YahooFinanceClient {
    private static final String CHART_URL = "https://query2.finance.yahoo.com/v8/finance/chart/%s"
            + "?period1=%d&period2=%d&interval=1d&events=history&includeAdjustedClose=true";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public YahooFinanceClient() {
        this(HttpClient.newHttpClient(), new ObjectMapper());
    }

    YahooFinanceClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public List<PriceBar> fetchDailyCloses(String symbol, LocalDate startDate, LocalDate endDate)
            throws IOException, InterruptedException {
        long period1 = startDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        long period2 = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        String encodedSymbol = URLEncoder.encode(symbol, StandardCharsets.UTF_8);
        URI uri = URI.create(CHART_URL.formatted(encodedSymbol, period1, period2));

        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Yahoo Finance request failed with HTTP " + response.statusCode());
        }

        return parseDailyCloses(response.body(), symbol);
    }

    public List<PriceBar> parseDailyCloses(String responseBody, String symbol) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode error = root.path("chart").path("error");
        if (!error.isMissingNode() && !error.isNull()) {
            throw new IOException("Yahoo Finance returned an error for " + symbol + ": " + error);
        }

        JsonNode result = root.path("chart").path("result").path(0);
        JsonNode timestamps = result.path("timestamp");
        JsonNode closes = result.path("indicators").path("quote").path(0).path("close");
        if (!timestamps.isArray() || !closes.isArray()) {
            throw new IOException("Yahoo Finance response did not contain daily close data for " + symbol);
        }

        List<PriceBar> bars = new ArrayList<>();
        int count = Math.min(timestamps.size(), closes.size());
        for (int i = 0; i < count; i++) {
            JsonNode close = closes.path(i);
            if (!close.isNull() && close.isNumber()) {
                bars.add(PriceBar.fromEpochSecond(timestamps.path(i).asLong(), close.asDouble()));
            }
        }
        if (bars.size() < 30) {
            throw new IOException("Not enough price history for " + symbol + "; got " + bars.size() + " daily bars");
        }
        return bars;
    }
}
