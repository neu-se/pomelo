package edu.neu.ccs.prl.pomelo.fuzz;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import org.junit.runners.model.MultipleFailureException;

import java.util.*;

class UnguidedQuickcheckFuzzer implements QuickcheckFuzzer {
    private final List<Throwable> failures = new LinkedList<>();
    private final Set<List<StackTraceElement>> traces = new HashSet<>();

    @Override
    public void setUp() {
    }

    @Override
    public void tearDown() throws MultipleFailureException {
        if (!failures.isEmpty()) {
            throw new MultipleFailureException(failures);
        }
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public void handleGenerateFailure(Throwable failure) {
        if (traces.add(Arrays.asList(failure.getStackTrace()))) {
            failures.add(failure);
        }
    }

    @Override
    public void handleGenerateSuccess(Object[] arguments) {
    }

    @Override
    public void handleTestSuccess() {
    }

    @Override
    public void handleTestFailure(Throwable failure) {
        if (traces.add(Arrays.asList(failure.getStackTrace()))) {
            failures.add(failure);
        }
    }

    @Override
    public void handleTestAssumptionFailure(Throwable failure) {
    }

    @Override
    public SourceOfRandomness next() {
        return null;
    }
}
