package edu.neu.ccs.prl.pomelo.fuzz;

import edu.neu.ccs.prl.pomelo.test.ParameterSupplier;

public interface Fuzzer extends ParameterSupplier {
    void setUp();

    void tearDown();

    void handleResult(Object[] arguments, Throwable failure);
}
