package com.ntnn.market.training;

import com.ntnn.market.domain.MarketAction;
import com.ntnn.market.domain.PriceBar;

import java.util.List;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class DeepLearning4JMarketPredictor {
    private static final int LOOKBACK_DAYS = 20;
    private static final int FEATURE_COUNT = 4;
    private static final int ACTION_COUNT = MarketAction.values().length;

    private final MultiLayerNetwork network;

    public DeepLearning4JMarketPredictor() {
        MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
                .seed(42)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.01))
                .list()
                .layer(new DenseLayer.Builder()
                        .nIn(FEATURE_COUNT)
                        .nOut(16)
                        .activation(Activation.RELU)
                        .build())
                .layer(new DenseLayer.Builder()
                        .nIn(16)
                        .nOut(8)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(8)
                        .nOut(ACTION_COUNT)
                        .activation(Activation.SOFTMAX)
                        .build())
                .build();

        network = new MultiLayerNetwork(configuration);
        network.init();
    }

    public void train(List<PriceBar> prices, int epochs) {
        if (prices.size() <= LOOKBACK_DAYS + 2) {
            throw new IllegalArgumentException("At least " + (LOOKBACK_DAYS + 3) + " prices are required");
        }
        if (epochs <= 0) {
            throw new IllegalArgumentException("epochs must be positive");
        }

        DataSet dataSet = toTrainingDataSet(prices);
        for (int epoch = 0; epoch < epochs; epoch++) {
            network.fit(dataSet);
        }
    }

    public DeepLearningMarketPrediction predict(String symbol, List<PriceBar> prices) {
        if (prices.size() <= LOOKBACK_DAYS) {
            throw new IllegalArgumentException("At least " + (LOOKBACK_DAYS + 1) + " prices are required");
        }

        INDArray features = Nd4j.create(new double[][] { featuresAt(prices, prices.size() - 1) });
        INDArray probabilities = network.output(features, false);
        double buy = probabilities.getDouble(0, MarketAction.BUY.ordinal());
        double hold = probabilities.getDouble(0, MarketAction.HOLD.ordinal());
        double sell = probabilities.getDouble(0, MarketAction.SELL.ordinal());
        MarketAction action = actionFor(maxIndex(buy, hold, sell));

        return new DeepLearningMarketPrediction(
                symbol,
                action,
                Math.max(buy, Math.max(hold, sell)),
                buy,
                hold,
                sell,
                prices.getLast().close());
    }

    private DataSet toTrainingDataSet(List<PriceBar> prices) {
        int rows = prices.size() - LOOKBACK_DAYS - 1;
        double[][] featureRows = new double[rows][FEATURE_COUNT];
        double[][] labelRows = new double[rows][ACTION_COUNT];

        for (int row = 0; row < rows; row++) {
            int priceIndex = LOOKBACK_DAYS + row;
            featureRows[row] = featuresAt(prices, priceIndex);
            MarketAction action = labelForNextReturn(prices, priceIndex);
            labelRows[row][action.ordinal()] = 1.0;
        }

        return new DataSet(Nd4j.create(featureRows), Nd4j.create(labelRows));
    }

    private double[] featuresAt(List<PriceBar> prices, int priceIndex) {
        return new double[] {
                returnOver(prices, priceIndex, 1),
                returnOver(prices, priceIndex, 3),
                returnOver(prices, priceIndex, 10),
                averageAbsoluteReturn(prices, priceIndex, 10)
        };
    }

    private MarketAction labelForNextReturn(List<PriceBar> prices, int priceIndex) {
        double todayClose = prices.get(priceIndex).close();
        double nextClose = prices.get(priceIndex + 1).close();
        double nextReturn = (nextClose - todayClose) / todayClose;
        if (nextReturn > 0.0025) {
            return MarketAction.BUY;
        }
        if (nextReturn < -0.0025) {
            return MarketAction.SELL;
        }
        return MarketAction.HOLD;
    }

    private double returnOver(List<PriceBar> prices, int priceIndex, int days) {
        double previous = prices.get(priceIndex - days).close();
        double current = prices.get(priceIndex).close();
        return (current - previous) / previous;
    }

    private double averageAbsoluteReturn(List<PriceBar> prices, int priceIndex, int days) {
        double total = 0.0;
        for (int i = priceIndex - days + 1; i <= priceIndex; i++) {
            double previous = prices.get(i - 1).close();
            double current = prices.get(i).close();
            total += Math.abs((current - previous) / previous);
        }
        return total / days;
    }

    private int maxIndex(double buy, double hold, double sell) {
        if (buy >= hold && buy >= sell) {
            return MarketAction.BUY.ordinal();
        }
        if (sell >= hold) {
            return MarketAction.SELL.ordinal();
        }
        return MarketAction.HOLD.ordinal();
    }

    private MarketAction actionFor(int index) {
        return MarketAction.values()[index];
    }
}
