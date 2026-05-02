package com.ntnn.unitTest;

import static org.assertj.core.api.Assertions.assertThat;

import com.ntnn.models.ReinforcementLearning;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ReinforcementLearningTest {
    @Test
    void updateAppliesQLearningFormula() {
        ReinforcementLearning<String, String> model = new ReinforcementLearning<>(0.5, 0.9, 0.0, new Random(1));

        model.update("next", "best", 1.0, "terminal", List.of());
        model.update("state", "action", 2.0, "next", List.of("best"));

        assertThat(model.qValue("state", "action")).isEqualTo(1.225);
    }

    @Test
    void bestActionReturnsHighestKnownQValue() {
        ReinforcementLearning<String, String> model = new ReinforcementLearning<>(0.5, 0.9, 0.0, new Random(1));

        model.update("state", "buy", 1.0, "next", List.of());
        model.update("state", "sell", -1.0, "next", List.of());

        assertThat(model.bestAction("state", List.of("buy", "hold", "sell"))).isEqualTo("buy");
    }
}
