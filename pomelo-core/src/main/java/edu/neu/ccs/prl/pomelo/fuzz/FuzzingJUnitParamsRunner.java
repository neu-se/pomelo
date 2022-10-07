package edu.neu.ccs.prl.pomelo.fuzz;

import junitparams.JUnitParamsRunner;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class FuzzingJUnitParamsRunner extends JUnitParamsRunner implements FuzzingTrialRunner {
    private final Fuzzer fuzzer;
    private final FrameworkMethod method;
    private Object[] arguments;

    public FuzzingJUnitParamsRunner(Class<?> clazz, String methodName, Fuzzer fuzzer) throws Throwable {
        super(clazz);
        this.fuzzer = fuzzer;
        this.method = FuzzingTrialRunner.getFrameworkMethod(getTestClass(), methodName);
    }

    @Override
    protected Statement childrenInvoker(RunNotifier notifier) {
        return new FuzzingStatement(fuzzer, this, getTestClass().getJavaClass(), method.getName());
    }

    @Override
    public void runTrial(RunNotifier notifier, Object[] arguments) {
        Description description = describeChild(method);
        this.arguments = arguments;
        Statement statement = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                methodBlock(method).evaluate();
            }
        };
        runLeaf(statement, description, notifier);
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod frameworkMethod, Object test) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                frameworkMethod.invokeExplosively(test, arguments);
            }
        };
    }
}
