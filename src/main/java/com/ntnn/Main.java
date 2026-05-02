package com.ntnn;

import com.ntnn.market.api.MarketSignalResponse;
import com.ntnn.market.service.MarketSignalService;
import com.ntnn.market.api.MarketTrainingRequest;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main implements CommandLineRunner {
    private final MarketSignalService marketSignalService;

    public Main(MarketSignalService marketSignalService) {
        this.marketSignalService = marketSignalService;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            return;
        }

        String modelType = args[0].equalsIgnoreCase("dl4j") ? "dl4j" : "q";
        String symbol = modelType.equals("dl4j")
                ? (args.length > 1 ? args[1] : "AAPL")
                : args[0];
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(5);
        int iterations = args.length > 2
                ? Integer.parseInt(args[2])
                : args.length > 1 && !modelType.equals("dl4j") ? Integer.parseInt(args[1]) : 500;

        MarketSignalResponse prediction = marketSignalService.trainAndPredict(
                new MarketTrainingRequest(symbol, modelType, startDate, endDate, iterations, false));
        System.out.printf(
                "Model=%s Symbol=%s lastClose=%.4f action=%s confidence=%s qValue=%s%n",
                prediction.modelType(),
                prediction.symbol(),
                prediction.lastClose(),
                prediction.status(),
                prediction.confidence(),
                prediction.qValue());
        System.out.println("This is a trading signal from historical data, not an exact market prediction.");
    }
}
