package edu.neu.ccs.prl.pomelo.fuzz;

import edu.neu.ccs.prl.pomelo.param.ParameterizedRunner;
import org.junit.runner.notification.RunNotifier;

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

    public TestExecutionResult run(Object[] arguments) throws Throwable {
        runner.runWithGroup(notifier, arguments);
        return listener.getTestResult();
    }
}
