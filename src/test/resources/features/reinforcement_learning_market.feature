Feature: Reinforcement learning market signals

  Scenario: Q-learning update uses the Bellman equation
    Given a Q-learning model with alpha 0.5 gamma 0.9 and epsilon 0.0
    And the next state has action "best" with learned reward 1.0
    When the model updates state "state" action "buy" with reward 2.0 and next state "next"
    Then the Q value for state "state" action "buy" should be 1.225

  Scenario: Rising historical prices produce a buy signal
    Given synthetic market prices that rise for 45 days
    When the market model trains for 100 episodes
    Then the latest market action should be BUY

  Scenario: DeepLearning4J learns a rising market as a buy signal
    Given synthetic market prices that rise for 45 days
    When the DeepLearning4J market model trains for 25 epochs
    Then the DeepLearning4J market action should be BUY
