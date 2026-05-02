package com.ntnn.unitTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ntnn.market.domain.MarketModelType;
import com.ntnn.market.training.MarketTrainingCommand;
import com.ntnn.market.api.MarketTrainingRequest;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class MarketTrainingCommandTest {
    @Test
    void fromNormalizesDefaultsAndSymbol() {
        LocalDate endDate = LocalDate.of(2026, 5, 2);
        MarketTrainingCommand command = MarketTrainingCommand.from(new MarketTrainingRequest(
                " aapl ",
                "dl4j",
                null,
                endDate,
                null,
                null));

        assertThat(command.symbol()).isEqualTo("AAPL");
        assertThat(command.modelType()).isEqualTo(MarketModelType.DL4J);
        assertThat(command.startDate()).isEqualTo(LocalDate.of(2021, 5, 2));
        assertThat(command.iterations()).isEqualTo(500);
        assertThat(command.refresh()).isFalse();
    }

    @Test
    void fromRejectsMissingSymbol() {
        assertThatThrownBy(() -> MarketTrainingCommand.from(new MarketTrainingRequest(
                null,
                "q",
                LocalDate.of(2021, 1, 1),
                LocalDate.of(2026, 1, 1),
                100,
                false)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("symbol is required");
    }
}
