package edu.neu.ccs.prl.pomelo.fuzz;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

class TestResultRunListener extends RunListener {
    private Throwable failure = null;
    private Throwable assumptionFailure = null;
    private TestExecutionResult result = null;

    @Override
    public void testStarted(Description description) {
        reset();
        result = null;
    }

    @Override
    public void testFinished(Description description) {
        if (assumptionFailure != null) {
            result = TestExecutionResult.assumptionFailure(assumptionFailure);
        } else if (failure != null) {
            result = TestExecutionResult.failed(failure);
        } else {
            result = TestExecutionResult.successful();
        }
        reset();
    }

    @Override
    public void testFailure(Failure failure) {
        this.failure = failure.getException();
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        this.assumptionFailure = failure.getException();
    }

    public TestExecutionResult getTestResult() {
        return result;
    }

    public void reset() {
        failure = null;
        assumptionFailure = null;
    }
}
