package edu.neu.ccs.prl.pomelo.fuzz.quickcheck;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.neu.ccs.prl.pomelo.fuzz.Fuzzer;

public class QuickcheckFuzzerAdapter implements Fuzzer {
    private final QuickcheckFuzzer fuzzer;
    private ArgumentsGenerator generator;

    public QuickcheckFuzzerAdapter(QuickcheckFuzzer fuzzer) {
        if (fuzzer == null) {
            throw new NullPointerException();
        }
        this.fuzzer = fuzzer;
    }

    @Override
    public void setUp(Class<?> testClass, String testMethodName) {
        generator = ArgumentsGenerator.create(testClass, testMethodName);
    }

    @Override
    public void tearDown() {
        fuzzer.tearDown();
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

    @Override
    public void handleResult(Object[] arguments, Throwable failure) {
        fuzzer.handleResult(arguments, failure);
    }
}
