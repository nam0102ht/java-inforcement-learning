package com.ntnn.unitTest;

import static org.assertj.core.api.Assertions.assertThat;

import com.ntnn.market.domain.PriceBar;
import com.ntnn.market.yahoo.YahooFinanceClient;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class YahooFinanceClientTest {
    @Test
    void parseDailyClosesSkipsNullCloseValues() throws IOException {
        StringBuilder timestamps = new StringBuilder();
        StringBuilder closes = new StringBuilder();
        for (int i = 0; i < 31; i++) {
            if (i > 0) {
                timestamps.append(',');
                closes.append(',');
            }
            timestamps.append(1_700_000_000L + i * 86_400L);
            closes.append(i == 3 ? "null" : 100 + i);
        }
        String body = """
                {
                  "chart": {
                    "result": [{
                      "timestamp": [%s],
                      "indicators": {"quote": [{"close": [%s]}]}
                    }],
                    "error": null
                  }
                }
                """.formatted(timestamps, closes);

        List<PriceBar> bars = new YahooFinanceClient().parseDailyCloses(body, "AAPL");

        assertThat(bars).hasSize(30);
        assertThat(bars.getFirst().date()).isEqualTo(LocalDate.of(2023, 11, 14));
        assertThat(bars).noneMatch(bar -> bar.close() == 103.0);
    }
}
