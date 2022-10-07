package edu.neu.ccs.prl.pomelo.fuzz;

import junitparams.JUnitParamsRunner;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized;

import java.io.File;

public final class FuzzForkMain {
    private FuzzForkMain() {
        throw new AssertionError();
    }

    public static void main(String[] args) throws Throwable {
        String testClassName = args[0];
        String testMethodName = args[1];
        File outputDir = new File(args[2]);
        ClassLoader testClassLoader = FuzzForkMain.class.getClassLoader();
        Class<?> testClass = java.lang.Class.forName(testClassName, true, testClassLoader);
        // TODO
        //run(testClass, testMethodName, outputDir);
    }

    public static void run(Class<?> testClass, String testMethodName, Fuzzer fuzzer) {
        Class<? extends Runner> runnerClass = getRunnerClass(testClass);
        if (runnerClass.equals(Parameterized.class)) {
            run(FuzzingParameterizedRunner::new, testClass, testMethodName, fuzzer);
        } else if (runnerClass.equals(JUnitParamsRunner.class)) {
            run(FuzzingJUnitParamsRunner::new, testClass, testMethodName, fuzzer);
        } else {
            throw new IllegalArgumentException("Unknown test class runner type:" + runnerClass);
        }
    }

    static Class<? extends Runner> getRunnerClass(Class<?> testClass) {
        if (!testClass.isAnnotationPresent(RunWith.class)) {
            throw new IllegalArgumentException("Test class must be annotated with RunWith: " + testClass);
        }
        RunWith annotation = testClass.getAnnotation(RunWith.class);
        return annotation.value();
    }

    static void run(FuzzingRunnerProducer builder, Class<?> clazz, String methodName, Fuzzer fuzzer) {
        try {
            Runner runner = build(builder, clazz, methodName, fuzzer);
            fuzzer.setUp(clazz, methodName);
            runner.run(new RunNotifier());
        } finally {
            fuzzer.tearDown();
        }
    }

    static Runner build(FuzzingRunnerProducer builder, Class<?> clazz, String methodName, Fuzzer fuzzer) {
        try {
            return builder.produce(clazz, methodName, fuzzer);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Unable to create JUnit runner for test: " + clazz + " " + methodName,
                                               e);
        }
    }
}