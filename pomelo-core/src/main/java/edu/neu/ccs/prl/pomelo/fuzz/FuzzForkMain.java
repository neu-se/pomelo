package edu.neu.ccs.prl.pomelo.fuzz;

import edu.neu.ccs.prl.pomelo.test.ParameterizedRunner;
import edu.neu.ccs.prl.pomelo.test.ParameterizedTestType;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

public final class FuzzForkMain {
    private FuzzForkMain() {
        throw new AssertionError();
    }

    public static void main(String[] args) throws Throwable {
        String testClassName = args[0];
        String testMethodName = args[1];
        ClassLoader testClassLoader = FuzzForkMain.class.getClassLoader();
        Class<?> testClass = java.lang.Class.forName(testClassName, true, testClassLoader);
        // TODO
        //run(testClass, testMethodName, outputDir);
    }

    public static void run(Class<?> testClass, String testMethodName, Fuzzer fuzzer, RunListener... listeners)
            throws Throwable {
        ParameterizedRunner runner = ParameterizedTestType.getType(testClass)
                                                          .wrap(testClass, testMethodName)
                                                          .createParameterizedRunner(fuzzer);
        RunNotifier notifier = new RunNotifier();
        for (RunListener listener : listeners) {
            notifier.addListener(listener);
        }
        // TODO connect failure listener to fuzzer
        notifier.addListener(new FailureListener());
        try {
            fuzzer.setUp();
            runner.run(notifier);
        } finally {
            fuzzer.tearDown();
        }
    }
}