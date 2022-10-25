package edu.neu.ccs.prl.pomelo.param;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public interface ParameterizedRunner {
    void run(RunNotifier notifier);

    void runWithGroup(RunNotifier notifier, Object[] group) throws InitializationError;

    default Statement createStatement(RunNotifier notifier, ParameterSupplier supplier) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                while (supplier.hasNext()) {
                    Object[] group = supplier.next();
                    if (group != null) {
                        runWithGroup(notifier, group);
                    }
                }
            }
        };
    }
}