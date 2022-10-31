package edu.neu.ccs.prl.pomelo.param;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

public interface ParameterizedRunner {
    void run(RunNotifier notifier);

    default void run() {
        run(new RunNotifier());
    }

    void runWithGroup(RunNotifier notifier, Object[] group) throws InitializationError;

    ParameterizedTest getParameterizedTest();
}