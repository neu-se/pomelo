package edu.neu.ccs.prl.pomelo.fuzz;

import edu.neu.ccs.prl.pomelo.param.ParameterizedRunner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.Statement;

import java.util.function.Consumer;

public final class StructuredFuzzTarget {
    private final TestResultRunListener listener = new TestResultRunListener();
    private final ParameterizedRunner runner;
    private final RunNotifier notifier;

    public StructuredFuzzTarget(ParameterizedRunner runner, RunNotifier notifier) {
        if (runner == null || notifier == null) {
            throw new NullPointerException();
        }
        this.runner = runner;
        this.notifier = notifier;
        notifier.addListener(listener);
    }

    public String getDescriptor() {
        return runner.getParameterizedTest().getDescriptor();
    }

    public TestExecutionResult run(Object[] arguments) throws Throwable {
        runner.runWithGroup(notifier, arguments);
        return listener.getTestResult();
    }

    public StructuredInputGenerator createGenerator() {
        return runner.getParameterizedTest().createGenerator();
    }

    public Statement createStatement(Fuzzer fuzzer, Consumer<Throwable> errorHandler) {
        return new Statement() {
            @Override
            public void evaluate() {
                try {
                    fuzzer.accept(StructuredFuzzTarget.this);
                } catch (Throwable t) {
                    errorHandler.accept(t);
                }
            }
        };
    }
}
