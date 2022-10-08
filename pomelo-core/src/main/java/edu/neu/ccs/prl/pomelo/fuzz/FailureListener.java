package edu.neu.ccs.prl.pomelo.fuzz;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class FailureListener extends RunListener {
    private Throwable failure = null;

    @Override
    public void testFinished(Description description) throws Exception {
        // TODO
        super.testFinished(description);
    }

    @Override
    public void testFailure(Failure failure) {
        this.failure = failure.getException();
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        this.failure = failure.getException();
    }
}
