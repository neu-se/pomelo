package edu.neu.ccs.prl.pomelo.fuzz;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

public interface QuickcheckFuzzer {
    void setUp();

    void tearDown();

    boolean hasNext();

    void handleGenerateFailure(Throwable error);

    void handleGenerateSuccess(Object[] arguments);

    void handleTestSuccess();

    void handleTestFailure(Throwable failure);

    void handleTestAssumptionFailure(Throwable failure);

    GenerationStatus createGenerationStatus(SourceOfRandomness source);

    SourceOfRandomness next();
}
