package com.ntnn.market.service;

import com.ntnn.market.domain.PriceBar;
import com.ntnn.market.persistence.MarketPriceBarRepository;
import com.ntnn.market.yahoo.YahooFinanceClient;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MarketTrainingDataService {
    private static final int MINIMUM_TRAINING_ROWS = 30;

    private final YahooFinanceClient yahooFinanceClient;
    private final MarketPriceBarRepository priceBarRepository;

    public MarketTrainingDataService(YahooFinanceClient yahooFinanceClient, MarketPriceBarRepository priceBarRepository) {
        this.yahooFinanceClient = yahooFinanceClient;
        this.priceBarRepository = priceBarRepository;
    }

    public List<PriceBar> loadTrainingData(String symbol, LocalDate startDate, LocalDate endDate, boolean refresh)
            throws IOException, InterruptedException {
        List<PriceBar> storedBars = priceBarRepository.findBySymbolAndDateRange(symbol, startDate, endDate);
        if (!refresh && storedBars.size() >= MINIMUM_TRAINING_ROWS) {
            return storedBars;
        }

        List<PriceBar> yahooBars = yahooFinanceClient.fetchDailyCloses(symbol, startDate, endDate);
        priceBarRepository.saveAll(symbol, yahooBars);
        return priceBarRepository.findBySymbolAndDateRange(symbol, startDate, endDate);
    }
}
