package edu.neu.ccs.prl.pomelo.fuzz;

public interface FuzzerBuilder {
    Fuzzer build(Class<?> testClass, String testMethodName, String[] arguments) throws Exception;
}
