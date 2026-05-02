package com.ntnn.unitTest;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ntnn.market.domain.MarketAction;
import com.ntnn.market.api.MarketSignalController;
import com.ntnn.market.api.MarketSignalResponse;
import com.ntnn.market.service.MarketSignalService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MarketSignalControllerTest {
    @Test
    void getSignalReturnsBuySellOrHoldStatus() throws Exception {
        MarketSignalService signalService = org.mockito.Mockito.mock(MarketSignalService.class);
        MarketSignalResponse response = new MarketSignalResponse(
                "AAPL",
                "Q_LEARNING",
                MarketAction.BUY,
                280.14,
                null,
                0.42,
                LocalDate.of(2021, 1, 1),
                LocalDate.of(2026, 1, 1),
                100,
                1000,
                LocalDateTime.of(2026, 5, 2, 9, 45));
        when(signalService.trainAndPredict(any())).thenReturn(response);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MarketSignalController(signalService)).build();

        mockMvc.perform(get("/api/market/signal").param("symbol", "AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol", is("AAPL")))
                .andExpect(jsonPath("$.modelType", is("Q_LEARNING")))
                .andExpect(jsonPath("$.status", is("BUY")));
    }

    @Test
    void trainAcceptsQueryParametersWithoutRequestBody() throws Exception {
        MarketSignalService signalService = org.mockito.Mockito.mock(MarketSignalService.class);
        MarketSignalResponse response = new MarketSignalResponse(
                "AAPL",
                "Q_LEARNING",
                MarketAction.HOLD,
                280.14,
                null,
                0.10,
                LocalDate.of(2021, 1, 1),
                LocalDate.of(2026, 1, 1),
                100,
                1000,
                LocalDateTime.of(2026, 5, 2, 9, 45));
        when(signalService.trainAndPredict(any())).thenReturn(response);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MarketSignalController(signalService)).build();

        mockMvc.perform(post("/api/market/train").param("symbol", "AAPL").param("iterations", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("HOLD")));
    }
}
