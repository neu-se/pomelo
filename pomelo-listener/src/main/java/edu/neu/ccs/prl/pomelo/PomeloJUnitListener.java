package edu.neu.ccs.prl.pomelo;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunListener.ThreadSafe;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@ThreadSafe
public class PomeloJUnitListener extends RunListener {
    private static final Set<Description> failingParameterizedTests = new HashSet<>();
    private static final Set<Description> parameterizedTests = new HashSet<>();
    private static final TestRecordWriter WRITER =
            new TestRecordWriter(new File(System.getProperty("pomelo.scan.report")));
    private static final String PROJECT = System.getProperty("pomelo.scan.project");
    private static final String PLUGIN = System.getProperty("pomelo.scan.plugin");
    private static final String EXECUTION = System.getProperty("pomelo.scan.execution");

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(PomeloJUnitListener::writeTestRecords));
    }

    @Override
    public void testFinished(Description description) {
        if (isParameterizedTest(description) || isJUnitParamsTest(description)) {
            synchronized (parameterizedTests) {
                parameterizedTests.add(description);
            }
        }
    }

    @Override
    public void testFailure(Failure failure) {
        Description description = failure.getDescription();
        if (isParameterizedTest(description) || isJUnitParamsTest(description)) {
            synchronized (failingParameterizedTests) {
                failingParameterizedTests.add(description);
            }
        }
    }

    private static void writeTestRecords() {
        Description[] failing;
        synchronized (failingParameterizedTests) {
            failing = failingParameterizedTests.toArray(new Description[0]);
            failingParameterizedTests.clear();
        }
        Description[] tests;
        synchronized (parameterizedTests) {
            tests = parameterizedTests.toArray(new Description[0]);
            parameterizedTests.clear();
        }
        Set<TestRecord> records = createRecords(tests);
        markAmbiguousTests(tests, records);
        markFailingTests(failing, records);
        synchronized (WRITER) {
            try {
                WRITER.appendAll(records);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    private static Set<TestRecord> createRecords(Description[] tests) {
        Set<TestRecord> records = new HashSet<>();
        for (Description test : tests) {
            String testClassName = test.getTestClass().getName();
            String testMethodName = getMethodName(test);
            String runnerClassName = test.getTestClass().getAnnotation(RunWith.class).value().getName();
            records.add(
                    new TestRecord(PROJECT, PLUGIN, EXECUTION, testClassName, testMethodName, runnerClassName, false,
                                   false));
        }
        return records;
    }

    private static void markFailingTests(Description[] failing, Set<TestRecord> records) {
        Set<TestRecord> failingRecords = createRecords(failing);
        for (TestRecord record : records) {
            if (failingRecords.contains(record)) {
                record.setFailed(true);
            }
        }
    }

    private static void markAmbiguousTests(Description[] tests, Set<TestRecord> records) {
        // All tests in a class are ambiguous if there is one than one test class with that name
        // A test method is ambiguous is there is more than one possible test method with the same name as the test
        // in its test class
        Map<String, Set<Class<?>>> classMap = new HashMap<>();
        for (Description test : tests) {
            String key = test.getTestClass().getName();
            if (!classMap.containsKey(key)) {
                classMap.put(key, new HashSet<>());
            }
            classMap.get(key).add(test.getTestClass());
        }
        for (TestRecord record : records) {
            String key = record.getTestClassName();
            if (classMap.get(key).size() != 1) {
                record.setAmbiguous(true);
            } else {
                Class<?> testClass = classMap.get(key).iterator().next();
                List<Method> methods = findMethod(testClass, record.getTestMethodName());
                if (methods.size() > 1) {
                    record.setAmbiguous(true);
                }
            }
        }
    }

    private static List<Method> findMethod(Class<?> clazz, String name) {
        return Arrays.stream(clazz.getMethods()).filter(m -> m.isAnnotationPresent(Test.class))
                     .filter(m -> m.getName().equals(name)).collect(Collectors.toList());
    }

    private static String getMethodName(Description description) {
        String methodName = description.getMethodName();
        int i;
        if ((i = methodName.indexOf('(')) != -1) {
            methodName = methodName.substring(0, i);
        }
        if ((i = methodName.indexOf('[')) != -1) {
            methodName = methodName.substring(0, i);
        }
        return methodName;
    }

    private static boolean isParameterizedTest(Description description) {
        Class<?> testClass = description.getTestClass();
        if (testClass != null && description.getMethodName() != null && testClass.isAnnotationPresent(RunWith.class)) {
            Class<? extends Runner> runnerClass = testClass.getAnnotation(RunWith.class).value();
            return runnerClass.equals(Parameterized.class);
        }
        return false;
    }

    private static boolean isJUnitParamsTest(Description description) {
        Class<?> testClass = description.getTestClass();
        if (testClass != null && description.getMethodName() != null && testClass.isAnnotationPresent(RunWith.class)) {
            Class<? extends Runner> runnerClass = testClass.getAnnotation(RunWith.class).value();
            return "junitparams.JUnitParamsRunner".equals(runnerClass.getName());
        }
        return false;
    }
}
