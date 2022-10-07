package edu.neu.ccs.prl.pomelo.fuzz;

import edu.neu.ccs.prl.pomelo.util.ParameterizedTestType;
import org.junit.runner.Runner;
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

    public static void run(Class<?> testClass, String testMethodName, Fuzzer fuzzer, RunListener... listeners) {
        Runner runner = createRunner(testClass, testMethodName, fuzzer);
        RunNotifier notifier = new RunNotifier();
        for (RunListener listener : listeners) {
            notifier.addListener(listener);
        }
        runner.run(notifier);
    }

    static Runner createRunner(Class<?> testClass, String testMethodName, Fuzzer fuzzer) {
        try {
            switch (ParameterizedTestType.findType(testClass)) {
                case JUNIT4_PARAMETERIZED:
                    return new FuzzingParameterizedRunner(testClass, testMethodName, fuzzer);
                case JUNIT_PARAMS:
                    return new FuzzingJUnitParamsRunner(testClass, testMethodName, fuzzer);
                default:
                    throw new AssertionError();
            }
        } catch (Throwable e) {
            throw new IllegalArgumentException(
                    "Unable to create JUnit runner for test: " + testClass + " " + testMethodName, e);
        }
    }
}