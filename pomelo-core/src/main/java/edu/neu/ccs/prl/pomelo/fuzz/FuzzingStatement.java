package edu.neu.ccs.prl.pomelo.fuzz;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

class FuzzingStatement extends Statement {
    private final Guidance guidance;
    private final ArgumentsGenerator generator;
    private final FuzzingTrialRunner runner;

    public FuzzingStatement(Guidance guidance, ArgumentsGenerator generator, FuzzingTrialRunner runner) {
        if (guidance == null || generator == null || runner == null) {
            throw new NullPointerException();
        }
        this.guidance = guidance;
        this.generator = generator;
        this.runner = runner;
    }

    public void evaluate() throws InitializationError {
        while (guidance.hasNext()) {
            Object[] arguments;
            try {
                arguments = generator.generate(guidance);
                guidance.handleGenerateSuccess(arguments);
            } catch (Throwable t) {
                guidance.handleGenerateFailure(t);
                continue;
            }
            FuzzingNotifier notifier = new FuzzingNotifier();
            runner.runTrial(notifier, arguments);
            guidance.handleResult(arguments, notifier.failure);
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
