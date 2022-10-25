package edu.neu.ccs.prl.pomelo.fuzz;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.neu.ccs.prl.pomelo.param.ParameterizedTestType;

public abstract class QuickcheckFuzzer implements Fuzzer {
    private final ArgumentsGenerator generator;

    public QuickcheckFuzzer(Class<?> testClass, String testMethodName) {
        this.generator = new ArgumentsGenerator(ParameterizedTestType.findAndWrap(testClass, testMethodName)
                                                                     .getParameterTypeContexts());
    }

    abstract void setUp();

    abstract void tearDown();

    abstract boolean hasNext();

    abstract void handleGenerateFailure(Throwable failure);

    abstract void handleGenerateSuccess(Object[] arguments);

    abstract void handleResult(TestExecutionResult result);

    abstract SourceOfRandomness next();

    abstract GenerationStatus createGenerationStatus(SourceOfRandomness source);

    @Override
    public void accept(StructuredFuzzTarget target) throws Throwable {
        setUp();
        try {
            while (hasNext()) {
                Object[] arguments = generateNext();
                if (arguments != null) {
                    handleResult(target.run(arguments));
                }
            }
        } finally {
            tearDown();
        }
    }

    private Object[] generateNext() {
        try {
            SourceOfRandomness source = next();
            Object[] arguments = generator.generate(source, createGenerationStatus(source));
            handleGenerateSuccess(arguments);
            return arguments;
        } catch (Throwable t) {
            handleGenerateFailure(t);
            return null;
        }
    }
}
