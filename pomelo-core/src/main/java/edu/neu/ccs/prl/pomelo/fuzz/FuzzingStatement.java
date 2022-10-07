package edu.neu.ccs.prl.pomelo.fuzz;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

class FuzzingStatement extends Statement {
    private final Fuzzer fuzzer;
    private final ArgumentsGenerator generator;
    private final FuzzingTrialRunner runner;

    public FuzzingStatement(Fuzzer fuzzer, ArgumentsGenerator generator, FuzzingTrialRunner runner) {
        if (fuzzer == null || generator == null || runner == null) {
            throw new NullPointerException();
        }
        this.fuzzer = fuzzer;
        this.generator = generator;
        this.runner = runner;
    }

    public void evaluate() throws InitializationError {
        while (fuzzer.hasNext()) {
            Object[] arguments;
            try {
                arguments = generator.generate(fuzzer);
                fuzzer.handleGenerateSuccess(arguments);
            } catch (Throwable t) {
                fuzzer.handleGenerateFailure(t);
                continue;
            }
            FuzzingNotifier notifier = new FuzzingNotifier();
            runner.runTrial(notifier, arguments);
            fuzzer.handleResult(arguments, notifier.failure);
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
