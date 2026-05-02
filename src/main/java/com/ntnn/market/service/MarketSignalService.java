package com.ntnn.market.service;

import com.ntnn.market.api.MarketSignalResponse;
import com.ntnn.market.api.MarketTrainingRequest;
import com.ntnn.market.domain.MarketModelType;
import com.ntnn.market.domain.PriceBar;
import com.ntnn.market.persistence.MarketPredictionRunRepository;
import com.ntnn.market.training.MarketModelTrainer;
import com.ntnn.market.training.MarketTrainingCommand;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MarketSignalService {
    private final MarketTrainingDataService trainingDataService;
    private final MarketPredictionRunRepository predictionRunRepository;
    private final Map<MarketModelType, MarketModelTrainer> trainers;

    public MarketSignalService(
            MarketTrainingDataService trainingDataService,
            MarketPredictionRunRepository predictionRunRepository,
            List<MarketModelTrainer> trainers) {
        this.trainingDataService = trainingDataService;
        this.predictionRunRepository = predictionRunRepository;
        this.trainers = new EnumMap<>(MarketModelType.class);
        trainers.forEach(trainer -> this.trainers.put(trainer.modelType(), trainer));
    }

    public MarketSignalResponse trainAndPredict(MarketTrainingRequest request)
            throws IOException, InterruptedException {
        MarketTrainingCommand command = MarketTrainingCommand.from(request);
        List<PriceBar> prices = trainingDataService.loadTrainingData(
                command.symbol(),
                command.startDate(),
                command.endDate(),
                command.refresh());
        MarketSignalResponse response = trainerFor(command.modelType()).train(command, prices);
        predictionRunRepository.save(response);
        return response;
    }

    private MarketModelTrainer trainerFor(MarketModelType modelType) {
        MarketModelTrainer trainer = trainers.get(modelType);
        if (trainer == null) {
            throw new IllegalArgumentException("Unsupported model type: " + modelType);
        }
        return trainer;
    }
}
