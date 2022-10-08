package edu.neu.ccs.prl.pomelo.fuzz;

import edu.neu.ccs.prl.pomelo.param.ParameterSupplier;
import org.junit.runner.notification.RunListener;
import org.junit.runners.model.MultipleFailureException;

public interface Fuzzer extends ParameterSupplier {
    void setUp();

    void tearDown() throws MultipleFailureException;

    RunListener getListener();
}
