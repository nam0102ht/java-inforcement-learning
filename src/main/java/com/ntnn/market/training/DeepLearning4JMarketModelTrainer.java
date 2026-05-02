package com.ntnn.market.training;

import com.ntnn.market.api.MarketSignalResponse;
import com.ntnn.market.domain.MarketModelType;
import com.ntnn.market.domain.PriceBar;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DeepLearning4JMarketModelTrainer implements MarketModelTrainer {
    @Override
    public MarketModelType modelType() {
        return MarketModelType.DL4J;
    }

    @Override
    public MarketSignalResponse train(MarketTrainingCommand command, List<PriceBar> prices) {
        DeepLearning4JMarketPredictor predictor = new DeepLearning4JMarketPredictor();
        predictor.train(prices, command.iterations());
        DeepLearningMarketPrediction prediction = predictor.predict(command.symbol(), prices);
        return new MarketSignalResponse(
                command.symbol(),
                modelType().name(),
                prediction.action(),
                prediction.lastClose(),
                prediction.confidence(),
                null,
                command.startDate(),
                command.endDate(),
                command.iterations(),
                prices.size(),
                LocalDateTime.now());
    }
}
