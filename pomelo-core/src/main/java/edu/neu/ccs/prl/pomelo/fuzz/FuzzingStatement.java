package edu.neu.ccs.prl.pomelo.fuzz;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

class FuzzingStatement extends Statement {
    private final Fuzzer fuzzer;
    private final FuzzingTrialRunner runner;
    private final Class<?> testClass;
    private final String testMethodName;

    public FuzzingStatement(Fuzzer fuzzer, FuzzingTrialRunner runner, Class<?> testClass, String testMethodName) {
        if (fuzzer == null || runner == null || testClass == null || testMethodName == null) {
            throw new NullPointerException();
        }
        this.fuzzer = fuzzer;
        this.runner = runner;
        this.testClass = testClass;
        this.testMethodName = testMethodName;
    }

    public void evaluate() throws InitializationError {
        try {
            fuzzer.setUp(testClass, testMethodName);
            while (fuzzer.hasNext()) {
                Object[] arguments = fuzzer.next();
                if (arguments != null) {
                    FuzzingNotifier notifier = new FuzzingNotifier();
                    runner.runTrial(notifier, arguments);
                    fuzzer.handleResult(arguments, notifier.failure);
                }
            }
        } finally {
            fuzzer.tearDown();
        }

    }

    private static final class FuzzingNotifier extends RunNotifier {
        Throwable failure;

        @Override
        public void fireTestFailure(Failure failure) {
            this.failure = failure.getException();
        }

        @Override
        public void fireTestAssumptionFailed(Failure failure) {
            this.failure = failure.getException();
        }
    }
}
