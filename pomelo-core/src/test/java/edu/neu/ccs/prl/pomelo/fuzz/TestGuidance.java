package edu.neu.ccs.prl.pomelo.fuzz;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.internal.GeometricDistribution;
import com.pholser.junit.quickcheck.internal.generator.SimpleGenerationStatus;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.util.Random;

final class TestGuidance implements Fuzzer {
    private int numberOfInputs;

    TestGuidance(int numberOfInputs) {
        this.numberOfInputs = numberOfInputs;
    }

    @Override
    public void setUp(Class<?> testClass, String testMethodName) {
    }

    @Override
    public void tearDown() {
    }

    @Override
    public boolean hasNext() {
        return numberOfInputs-- > 0;
    }

    @Override
    public void handleGenerateFailure(Throwable error) {
    }

    @Override
    public void handleGenerateSuccess(Object[] arguments) {
    }

    @Override
    public void handleResult(Object[] arguments, Throwable error) {
    }

    @Override
    public GenerationStatus createGenerationStatus(SourceOfRandomness source) {
        return new SimpleGenerationStatus(new GeometricDistribution(), source, 10);
    }

    @Override
    public SourceOfRandomness next() {
        return new SourceOfRandomness(new Random(8L));
    }
}
