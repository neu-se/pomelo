package edu.neu.ccs.prl.pomelo.fuzz;

import org.junit.runner.Runner;

public interface FuzzingRunnerProducer {
    Runner produce(Class<?> clazz, String methodName, Guidance guidance) throws Throwable;
}
