package edu.neu.ccs.prl.pomelo.fuzz;

public final class TestExecutionResult {
    private static final TestExecutionResult SUCCESSFUL_RESULT = new TestExecutionResult(Status.SUCCESSFUL, null);
    /**
     * The status of the test execution.
     * <p>
     * Non-null.
     */
    private final Status status;
    /**
     * If the test's status was {@link Status#FAILED} or {@link Status#ASSUMPTION_FAILURE}, the failure. Otherwise,
     * {@code null}.
     */
    private final Throwable failure;

    private TestExecutionResult(TestExecutionResult.Status status, Throwable failure) {
        this.status = status;
        this.failure = failure;
    }

    public TestExecutionResult.Status getStatus() {
        return status;
    }

    public Throwable getFailure() {
        return failure;
    }

    @Override
    public String toString() {
        return String.format("TestResult{status=%s, failure=%s}", status, failure);
    }

    public static TestExecutionResult successful() {
        return SUCCESSFUL_RESULT;
    }

    public static TestExecutionResult failed(Throwable failure) {
        return new TestExecutionResult(Status.FAILED, failure);
    }

    public static TestExecutionResult assumptionFailure(Throwable failure) {
        return new TestExecutionResult(Status.ASSUMPTION_FAILURE, failure);
    }

    public enum Status {
        /**
         * Indicates that a test execution was successful.
         */
        SUCCESSFUL,
        /**
         * Indicates that a test execution produced a failure.
         */
        FAILED,
        /**
         * Indicates that a test execution produced an assumption failure.
         */
        ASSUMPTION_FAILURE
    }
}
