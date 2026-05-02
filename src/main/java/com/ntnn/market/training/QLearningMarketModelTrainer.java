package com.ntnn.market.training;

import com.ntnn.market.api.MarketSignalResponse;
import com.ntnn.market.domain.MarketAction;
import com.ntnn.market.domain.MarketModelType;
import com.ntnn.market.domain.MarketState;
import com.ntnn.market.domain.PriceBar;
import com.ntnn.market.persistence.MarketQValueRepository;

import com.ntnn.models.ReinforcementLearning;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class QLearningMarketModelTrainer implements MarketModelTrainer {
    private static final List<MarketAction> ACTIONS = List.of(MarketAction.BUY, MarketAction.HOLD, MarketAction.SELL);

    private final MarketQValueRepository qValueRepository;

    public QLearningMarketModelTrainer(MarketQValueRepository qValueRepository) {
        this.qValueRepository = qValueRepository;
    }

    @Override
    public MarketModelType modelType() {
        return MarketModelType.Q_LEARNING;
    }

    @Override
    public MarketSignalResponse train(MarketTrainingCommand command, List<PriceBar> prices) {
        MarketEnvironment environment = new MarketEnvironment(prices);
        ReinforcementLearning<MarketState, MarketAction> model = new ReinforcementLearning<>(0.15, 0.90, 0.10);
        loadStoredQValues(command.symbol(), model);
        model.train(environment, command.iterations());
        qValueRepository.saveAll(command.symbol(), model.snapshot());

        MarketState latestState = environment.latestState();
        MarketAction action = model.bestAction(latestState, ACTIONS);
        return new MarketSignalResponse(
                command.symbol(),
                modelType().name(),
                action,
                environment.latestClose(),
                null,
                model.qValue(latestState, action),
                command.startDate(),
                command.endDate(),
                command.iterations(),
                prices.size(),
                LocalDateTime.now());
    }

    private void loadStoredQValues(String symbol, ReinforcementLearning<MarketState, MarketAction> model) {
        for (MarketQValueRepository.StoredQValue storedQValue : qValueRepository.findBySymbol(symbol)) {
            model.setQValue(storedQValue.state(), storedQValue.action(), storedQValue.qValue());
        }
    }
}
