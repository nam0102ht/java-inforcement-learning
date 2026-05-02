package com.ntnn.blackboxTest.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.ntnn.market.training.DeepLearning4JMarketPredictor;
import com.ntnn.market.training.DeepLearningMarketPrediction;
import com.ntnn.market.domain.MarketAction;
import com.ntnn.market.training.MarketEnvironment;
import com.ntnn.market.domain.MarketState;
import com.ntnn.market.domain.PriceBar;
import com.ntnn.models.ReinforcementLearning;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReinforcementLearningMarketSteps {
    private ReinforcementLearning<String, String> textModel;
    private ReinforcementLearning<MarketState, MarketAction> marketModel;
    private MarketEnvironment marketEnvironment;
    private List<PriceBar> syntheticPrices;
    private MarketAction latestAction;
    private DeepLearningMarketPrediction deepLearningPrediction;

    @Given("a Q-learning model with alpha {double} gamma {double} and epsilon {double}")
    public void aQLearningModelWithAlphaGammaAndEpsilon(double alpha, double gamma, double epsilon) {
        textModel = new ReinforcementLearning<>(alpha, gamma, epsilon, new Random(1));
    }

    @Given("the next state has action {string} with learned reward {double}")
    public void theNextStateHasActionWithLearnedReward(String action, double reward) {
        textModel.update("next", action, reward, "terminal", List.of());
    }

    @When("the model updates state {string} action {string} with reward {double} and next state {string}")
    public void theModelUpdatesStateActionWithRewardAndNextState(
            String state,
            String action,
            double reward,
            String nextState) {
        textModel.update(state, action, reward, nextState, List.of("best"));
    }

    @Then("the Q value for state {string} action {string} should be {double}")
    public void theQValueForStateActionShouldBe(String state, String action, double expectedValue) {
        assertThat(textModel.qValue(state, action)).isEqualTo(expectedValue);
    }

    @Given("synthetic market prices that rise for {int} days")
    public void syntheticMarketPricesThatRiseForDays(int days) {
        List<PriceBar> prices = new ArrayList<>();
        LocalDate firstDate = LocalDate.of(2024, 1, 1);
        double close = 100.0;
        for (int day = 0; day < days; day++) {
            prices.add(new PriceBar(firstDate.plusDays(day), close));
            close += 1.0;
        }
        syntheticPrices = List.copyOf(prices);
        marketEnvironment = new MarketEnvironment(prices);
        marketModel = new ReinforcementLearning<>(0.20, 0.90, 0.0, new Random(1));
    }

    @When("the market model trains for {int} episodes")
    public void theMarketModelTrainsForEpisodes(int episodes) {
        marketModel.train(marketEnvironment, episodes);
        latestAction = marketModel.bestAction(
                marketEnvironment.latestState(),
                List.of(MarketAction.BUY, MarketAction.HOLD, MarketAction.SELL));
    }

    @Then("the latest market action should be BUY")
    public void theLatestMarketActionShouldBeBuy() {
        assertThat(latestAction).isEqualTo(MarketAction.BUY);
    }

    @When("the DeepLearning4J market model trains for {int} epochs")
    public void theDeepLearning4JMarketModelTrainsForEpochs(int epochs) {
        DeepLearning4JMarketPredictor predictor = new DeepLearning4JMarketPredictor();
        predictor.train(syntheticPrices, epochs);
        deepLearningPrediction = predictor.predict("SYNTH", syntheticPrices);
    }

    @Then("the DeepLearning4J market action should be BUY")
    public void theDeepLearning4JMarketActionShouldBeBuy() {
        assertThat(deepLearningPrediction.action()).isEqualTo(MarketAction.BUY);
    }
}
