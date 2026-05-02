package com.ntnn.env;

import java.util.List;

public interface Environment<S, A> {
    S reset();

    StepResult<S> step(A action);

    List<A> getAvailableActions(S state);

    boolean isTerminal(S state);
}
