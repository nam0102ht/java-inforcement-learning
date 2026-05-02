package com.ntnn.env;

public record StepResult<S>(S nextState, double reward, boolean done) {
}
