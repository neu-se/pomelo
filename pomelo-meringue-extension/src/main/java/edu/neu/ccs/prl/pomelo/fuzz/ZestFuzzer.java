package edu.neu.ccs.prl.pomelo.fuzz;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.berkeley.cs.jqf.fuzz.guidance.*;
import edu.berkeley.cs.jqf.fuzz.junit.quickcheck.FastSourceOfRandomness;
import edu.berkeley.cs.jqf.fuzz.junit.quickcheck.NonTrackingGenerationStatus;
import edu.berkeley.cs.jqf.instrument.InstrumentationException;
import edu.berkeley.cs.jqf.instrument.tracing.SingleSnoop;
import edu.berkeley.cs.jqf.instrument.tracing.TraceLogger;
import org.junit.AssumptionViolatedException;
import org.junit.runners.model.MultipleFailureException;

import java.io.EOFException;
import java.util.*;

final class ZestFuzzer extends QuickcheckFuzzer {
    private final Guidance guidance;
    private final List<Throwable> failures = new LinkedList<>();
    private final Set<List<StackTraceElement>> traces = new HashSet<>();
    private final Class<?> testClass;
    private final String testMethodName;

    public ZestFuzzer(Class<?> testClass, String testMethodName, Guidance guidance) {
        super(testClass, testMethodName);
        if (testClass == null || testMethodName == null || guidance == null) {
            throw new NullPointerException();
        }
        this.testClass = testClass;
        this.testMethodName = testMethodName;
        this.guidance = guidance;
    }

    @Override
    public void setUp() {
        SingleSnoop.setCallbackGenerator(guidance::generateCallBack);
        SingleSnoop.startSnooping(testClass.getName() + "#" + testMethodName);
    }

    @Override
    public void tearDown() {
        if (!failures.isEmpty()) {
            new MultipleFailureException(failures).printStackTrace();
        }
        TraceLogger.get().remove();
    }

    @Override
    public boolean hasNext() {
        return guidance.hasInput();
    }

    @Override
    public void handleGenerateFailure(Throwable failure) {
        if (failure instanceof IllegalStateException && failure.getCause() instanceof EOFException) {
            guidance.handleResult(Result.INVALID,
                                  new AssumptionViolatedException("StreamBackedRandom does not have enough data",
                                                                  failure.getCause()));
        }
        handleTestFailure(failure);
    }

    @Override
    public void handleGenerateSuccess(Object[] arguments) {
        guidance.observeGeneratedArgs(arguments);
    }

    @Override
    public void handleResult(TestExecutionResult result) {
        switch (result.getStatus()) {
            case SUCCESSFUL:
                guidance.handleResult(Result.SUCCESS, null);
                return;
            case ASSUMPTION_FAILURE:
                guidance.handleResult(Result.INVALID, result.getFailure());
                return;
            case FAILED:
                handleTestFailure(result.getFailure());
        }
    }

    @Override
    public SourceOfRandomness next() {
        return new FastSourceOfRandomness(new StreamBackedRandom(guidance.getInput(), Long.BYTES));
    }

    @Override
    public GenerationStatus createGenerationStatus(SourceOfRandomness source) {
        return new NonTrackingGenerationStatus(source);
    }

    private void handleTestFailure(Throwable failure) {
        if (failure instanceof InstrumentationException) {
            throw new GuidanceException(failure);
        } else if (failure instanceof GuidanceException) {
            throw (GuidanceException) failure;
        } else if (failure instanceof TimeoutException) {
            guidance.handleResult(Result.TIMEOUT, failure);
        } else if (failure instanceof AssumptionViolatedException) {
            guidance.handleResult(Result.INVALID, failure);
        } else {
            if (traces.add(Arrays.asList(failure.getStackTrace()))) {
                failures.add(failure);
            }
            guidance.handleResult(Result.FAILURE, failure);
        }
    }
}
