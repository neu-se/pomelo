package edu.neu.ccs.prl.pomelo.test;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public interface ParameterizedRunner {
    void run(RunNotifier notifier);

    void runWithParameterGroup(RunNotifier notifier, Object[] parameterGroup) throws InitializationError;

    default Statement createStatement(RunNotifier notifier, ParameterSupplier supplier) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                while (supplier.hasNext()) {
                    Object[] group = supplier.next();
                    if (group != null) {
                        runWithParameterGroup(notifier, group);
                    }
                }
            }
        };
    }
}