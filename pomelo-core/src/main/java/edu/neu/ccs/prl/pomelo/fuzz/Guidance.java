package edu.neu.ccs.prl.pomelo.fuzz;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

public interface Guidance {
    void setUp(Class<?> testClass, String testMethodName);

    void tearDown();

    boolean hasNext();

    void handleGenerateFailure(Throwable error);

    void handleGenerateSuccess(Object[] arguments);

    void handleResult(Object[] arguments, Throwable error);

    GenerationStatus createGenerationStatus(SourceOfRandomness source);

    SourceOfRandomness next();
}
