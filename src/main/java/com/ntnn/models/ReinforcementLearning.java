package com.ntnn.models;

import com.ntnn.env.Environment;
import com.ntnn.env.StepResult;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class ReinforcementLearning<S, A> {
    private final double alpha;
    private final double gamma;
    private final double epsilon;
    private final Random random;
    private final Map<QEntry<S, A>, Double> qTable = new HashMap<>();

    public ReinforcementLearning(double alpha, double gamma, double epsilon) {
        this(alpha, gamma, epsilon, new Random());
    }

    public ReinforcementLearning(double alpha, double gamma, double epsilon, Random random) {
        validateRate(alpha, "alpha");
        validateRate(gamma, "gamma");
        validateRate(epsilon, "epsilon");
        this.alpha = alpha;
        this.gamma = gamma;
        this.epsilon = epsilon;
        this.random = Objects.requireNonNull(random);
    }

    public void train(Environment<S, A> environment, int episodes) {
        if (episodes <= 0) {
            throw new IllegalArgumentException("episodes must be positive");
        }

        for (int episode = 0; episode < episodes; episode++) {
            S state = environment.reset();
            while (!environment.isTerminal(state)) {
                A action = chooseTrainingAction(environment, state);
                StepResult<S> result = environment.step(action);
                update(state, action, result.reward(), result.nextState(), environment.getAvailableActions(result.nextState()));
                state = result.nextState();
                if (result.done()) {
                    break;
                }
            }
        }
    }

    public A bestAction(S state, List<A> availableActions) {
        if (availableActions.isEmpty()) {
            throw new IllegalArgumentException("availableActions must not be empty");
        }
        return availableActions.stream()
                .max(Comparator.comparingDouble(action -> qValue(state, action)))
                .orElseThrow();
    }

    public double qValue(S state, A action) {
        return qTable.getOrDefault(new QEntry<>(state, action), 0.0);
    }

    public void setQValue(S state, A action, double qValue) {
        qTable.put(new QEntry<>(state, action), qValue);
    }

    public void update(S state, A action, double reward, S nextState, List<A> nextActions) {
        double oldValue = qValue(state, action);
        double maxNextQ = nextActions.stream()
                .mapToDouble(nextAction -> qValue(nextState, nextAction))
                .max()
                .orElse(0.0);

        // Q(s,a) <- Q(s,a) + alpha * [r + gamma * max Q(s',a') - Q(s,a)]
        double newValue = oldValue + alpha * (reward + gamma * maxNextQ - oldValue);
        qTable.put(new QEntry<>(state, action), newValue);
    }

    public Map<QEntry<S, A>, Double> snapshot() {
        return Map.copyOf(qTable);
    }

    private A chooseTrainingAction(Environment<S, A> environment, S state) {
        List<A> actions = environment.getAvailableActions(state);
        if (actions.isEmpty()) {
            throw new IllegalStateException("Environment returned no actions");
        }
        if (random.nextDouble() < epsilon) {
            return actions.get(random.nextInt(actions.size()));
        }
        return bestAction(state, actions);
    }

    private void validateRate(double value, String name) {
        if (Double.isNaN(value) || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " must be between 0 and 1");
        }
    }

    public record QEntry<S, A>(S state, A action) {
    }
}
