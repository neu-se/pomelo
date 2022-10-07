package edu.neu.ccs.prl.pomelo.fuzz;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParameters;
import org.junit.runners.parameterized.TestWithParameters;

import java.util.Arrays;

public class FuzzingParameterizedRunner extends Parameterized implements FuzzingTrialRunner {
    private final Fuzzer fuzzer;
    private final FrameworkMethod method;

    public FuzzingParameterizedRunner(Class<?> clazz, String methodName, Fuzzer fuzzer) throws Throwable {
        super(clazz);
        this.fuzzer = fuzzer;
        this.method = FuzzingTrialRunner.getFrameworkMethod(getTestClass(), methodName);
    }

    @Override
    protected Statement childrenInvoker(RunNotifier notifier) {
        return new FuzzingStatement(fuzzer, this, getTestClass().getJavaClass(), method.getName());
    }

    @Override
    public void runTrial(RunNotifier notifier, Object[] arguments) throws InitializationError {
        new TrialRunner(getTestClass(), method, arguments).run(notifier);
    }

    private static class TrialRunner extends BlockJUnit4ClassRunnerWithParameters {
        private final FrameworkMethod method;

        TrialRunner(TestClass testClass, FrameworkMethod method, Object[] arguments) throws InitializationError {
            super(new TestWithParameters("[0]", testClass, Arrays.asList(arguments)));
            this.method = method;
        }

        @Override
        protected void runChild(FrameworkMethod method, RunNotifier notifier) {
            if (method.equals(this.method)) {
                super.runChild(method, notifier);
            }
        }
    }
}
