package com.ntnn.market.api;

import com.ntnn.market.service.MarketSignalService;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market")
public class MarketSignalController {
    private final MarketSignalService marketSignalService;

    public MarketSignalController(MarketSignalService marketSignalService) {
        this.marketSignalService = marketSignalService;
    }

    @GetMapping("/signal")
    public MarketSignalResponse getSignal(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "q") String model,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "500") int iterations,
            @RequestParam(defaultValue = "false") boolean refresh) throws Exception {
        return marketSignalService.trainAndPredict(
                new MarketTrainingRequest(symbol, model, startDate, endDate, iterations, refresh));
    }

    @PostMapping("/train")
    public MarketSignalResponse train(
            @RequestBody(required = false) MarketTrainingRequest request,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false, defaultValue = "q") String model,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer iterations,
            @RequestParam(required = false) Boolean refresh) throws Exception {
        if (request != null) {
            return marketSignalService.trainAndPredict(request);
        }
        return marketSignalService.trainAndPredict(
                new MarketTrainingRequest(symbol, model, startDate, endDate, iterations, refresh));
    }
}
