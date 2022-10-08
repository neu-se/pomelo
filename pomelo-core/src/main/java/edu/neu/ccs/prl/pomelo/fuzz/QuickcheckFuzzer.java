package edu.neu.ccs.prl.pomelo.fuzz;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import org.junit.runners.model.MultipleFailureException;

public interface QuickcheckFuzzer {
    void setUp();

    void tearDown() throws MultipleFailureException;

    boolean hasNext();

    void handleGenerateFailure(Throwable failure);

    void handleGenerateSuccess(Object[] arguments);

    void handleTestSuccess();

    void handleTestFailure(Throwable failure);

    void handleTestAssumptionFailure(Throwable failure);

    SourceOfRandomness next();
}
