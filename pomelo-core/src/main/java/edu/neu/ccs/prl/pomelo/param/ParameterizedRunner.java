package edu.neu.ccs.prl.pomelo.param;

import edu.neu.ccs.prl.pomelo.fuzz.Fuzzer;
import edu.neu.ccs.prl.pomelo.fuzz.StructuredFuzzTarget;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public interface ParameterizedRunner {
    void run(RunNotifier notifier);

    default void run() {
        run(new RunNotifier());
    }

    void runWithGroup(RunNotifier notifier, Object[] group) throws InitializationError;

    default Statement createStatement(RunNotifier notifier, Fuzzer fuzzer) {
        return new StructuredFuzzTarget(this, notifier).createStatement(fuzzer,
                                                                        (t) -> {
                                                                            throw new RuntimeException(t);
                                                                        });
    }

    ParameterizedTest getParameterizedTest();
}