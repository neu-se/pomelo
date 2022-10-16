package edu.neu.ccs.prl.pomelo.fuzz;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.neu.ccs.prl.pomelo.param.ParameterSupplier;
import edu.neu.ccs.prl.pomelo.param.ParameterizedTestType;
import edu.neu.ccs.prl.pomelo.scan.TestMethod;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runners.model.MultipleFailureException;

public class FuzzerAdapter implements ParameterSupplier {
    private final QuickcheckFuzzer fuzzer;
    private final ArgumentsGenerator generator;
    private final RunListener listener;

    public FuzzerAdapter(QuickcheckFuzzer fuzzer, Class<?> testClass, String testMethodName) {
        if (fuzzer == null) {
            throw new NullPointerException();
        }
        this.fuzzer = fuzzer;
        this.generator = new ArgumentsGenerator(
                ParameterizedTestType.wrap(new TestMethod(testClass, testMethodName)).getParameterTypeContexts());
        this.listener = new FailureListener();
    }

    public void setUp() {
        fuzzer.setUp();
    }

    public void tearDown() throws MultipleFailureException {
        fuzzer.tearDown();
    }

    public RunListener getListener() {
        return listener;
    }

    @Override
    public boolean hasNext() {
        return fuzzer.hasNext();
    }

    @Override
    public Object[] next() {
        try {
            SourceOfRandomness source = fuzzer.next();
            Object[] arguments = generator.generate(source, fuzzer.createGenerationStatus(source));
            fuzzer.handleGenerateSuccess(arguments);
            return arguments;
        } catch (Throwable t) {
            fuzzer.handleGenerateFailure(t);
            return null;
        }
    }

    private class FailureListener extends RunListener {
        private Throwable failure = null;
        private Throwable assumptionFailure = null;

        @Override
        public void testStarted(Description description) {
            failure = null;
            assumptionFailure = null;
        }

        @Override
        public void testFinished(Description description) {
            if (assumptionFailure != null) {
                fuzzer.handleTestAssumptionFailure(assumptionFailure);
            } else if (failure != null) {
                fuzzer.handleTestFailure(failure);
            } else {
                fuzzer.handleTestSuccess();
            }
            failure = null;
            assumptionFailure = null;
        }

        @Override
        public void testFailure(Failure failure) {
            this.failure = failure.getException();
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            this.assumptionFailure = failure.getException();
        }
    }
}
