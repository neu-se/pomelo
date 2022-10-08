package edu.neu.ccs.prl.pomelo.fuzz;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.neu.ccs.prl.pomelo.test.ParameterSupplier;

public class QuickcheckFuzzerAdapter implements ParameterSupplier {
    private final QuickcheckFuzzer fuzzer;
    private final ArgumentsGenerator generator;

    public QuickcheckFuzzerAdapter(QuickcheckFuzzer fuzzer, Class<?> testClass, String testMethodName) {
        if (fuzzer == null) {
            throw new NullPointerException();
        }
        this.fuzzer = fuzzer;
        this.generator = ArgumentsGenerator.create(testClass, testMethodName);
    }

    public void setUp() {
        fuzzer.setUp();
    }

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
}
