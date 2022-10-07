package edu.neu.ccs.prl.pomelo.fuzz;

public interface Fuzzer {
    void setUp(Class<?> testClass, String testMethodName);

    void tearDown();

    boolean hasNext();

    Object[] next();

    void handleResult(Object[] arguments, Throwable failure);
}
