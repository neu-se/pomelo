package edu.neu.ccs.prl.pomelo.fuzz;

public interface Fuzzer {
    void accept(StructuredFuzzTarget target) throws Throwable;
}