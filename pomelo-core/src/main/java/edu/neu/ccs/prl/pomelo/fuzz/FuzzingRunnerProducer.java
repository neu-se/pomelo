package edu.neu.ccs.prl.pomelo.fuzz;

import edu.neu.ccs.prl.pomelo.fuzz.quickcheck.QuickcheckFuzzer;
import org.junit.runner.Runner;

public interface FuzzingRunnerProducer {
    Runner produce(Class<?> clazz, String methodName, QuickcheckFuzzer fuzzer) throws Throwable;
}
