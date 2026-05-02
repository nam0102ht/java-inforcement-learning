package com.ntnn.market.training;

import com.ntnn.market.api.MarketSignalResponse;
import com.ntnn.market.domain.MarketModelType;
import com.ntnn.market.domain.PriceBar;

import java.util.List;

public interface MarketModelTrainer {
    MarketModelType modelType();

    MarketSignalResponse train(MarketTrainingCommand command, List<PriceBar> prices);
}
