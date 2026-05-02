package com.ntnn.market.training;

import com.ntnn.market.domain.MarketAction;
import com.ntnn.market.domain.MarketState;
import com.ntnn.market.domain.PriceBar;

import com.ntnn.env.Environment;
import com.ntnn.env.StepResult;
import java.util.List;

public class MarketEnvironment implements Environment<MarketState, MarketAction> {
    private static final int MIN_INDEX = 20;
    private static final List<MarketAction> ACTIONS = List.of(MarketAction.BUY, MarketAction.HOLD, MarketAction.SELL);

    private final List<PriceBar> prices;
    private int index;

    public MarketEnvironment(List<PriceBar> prices) {
        if (prices.size() <= MIN_INDEX + 2) {
            throw new IllegalArgumentException("At least " + (MIN_INDEX + 3) + " prices are required");
        }
        this.prices = List.copyOf(prices);
        this.index = MIN_INDEX;
    }

    @Override
    public MarketState reset() {
        index = MIN_INDEX;
        return stateAt(index);
    }

    @Override
    public StepResult<MarketState> step(MarketAction action) {
        if (isTerminal(stateAt(index))) {
            return new StepResult<>(stateAt(index), 0.0, true);
        }

        double todayClose = prices.get(index).close();
        double nextClose = prices.get(index + 1).close();
        double nextReturn = (nextClose - todayClose) / todayClose;
        double reward = rewardFor(action, nextReturn);
        index++;
        MarketState nextState = stateAt(index);
        return new StepResult<>(nextState, reward, isTerminal(nextState));
    }

    @Override
    public List<MarketAction> getAvailableActions(MarketState state) {
        return ACTIONS;
    }

    @Override
    public boolean isTerminal(MarketState state) {
        return index >= prices.size() - 2;
    }

    public MarketState latestState() {
        return stateAt(prices.size() - 1);
    }

    public double latestClose() {
        return prices.getLast().close();
    }

    private double rewardFor(MarketAction action, double nextReturn) {
        return switch (action) {
            case BUY -> nextReturn;
            case SELL -> -nextReturn;
            case HOLD -> -Math.abs(nextReturn) * 0.05;
        };
    }

    private MarketState stateAt(int priceIndex) {
        double shortReturn = returnOver(priceIndex, 3);
        double mediumReturn = returnOver(priceIndex, 10);
        double volatility = averageAbsoluteReturn(priceIndex, 10);
        return new MarketState(trendBucket(shortReturn), trendBucket(mediumReturn), volatilityBucket(volatility));
    }

    private double returnOver(int priceIndex, int days) {
        double previous = prices.get(priceIndex - days).close();
        double current = prices.get(priceIndex).close();
        return (current - previous) / previous;
    }

    private double averageAbsoluteReturn(int priceIndex, int days) {
        double total = 0.0;
        for (int i = priceIndex - days + 1; i <= priceIndex; i++) {
            double previous = prices.get(i - 1).close();
            double current = prices.get(i).close();
            total += Math.abs((current - previous) / previous);
        }
        return total / days;
    }

    private MarketState.TrendBucket trendBucket(double value) {
        if (value > 0.005) {
            return MarketState.TrendBucket.UP;
        }
        if (value < -0.005) {
            return MarketState.TrendBucket.DOWN;
        }
        return MarketState.TrendBucket.FLAT;
    }

    private MarketState.VolatilityBucket volatilityBucket(double value) {
        if (value < 0.01) {
            return MarketState.VolatilityBucket.LOW;
        }
        if (value < 0.025) {
            return MarketState.VolatilityBucket.MEDIUM;
        }
        return MarketState.VolatilityBucket.HIGH;
    }
}
