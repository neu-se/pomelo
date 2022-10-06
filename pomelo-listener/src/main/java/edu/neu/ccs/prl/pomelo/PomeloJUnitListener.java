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
    private static final EntryWriter WRITER = new EntryWriter(new File(System.getProperty("pomelo.scan.report")));

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
        Description[] failingTests;
        synchronized (failingParameterizedTests) {
            failingTests = failingParameterizedTests.toArray(new Description[0]);
            failingParameterizedTests.clear();
        }
        Description[] tests;
        synchronized (parameterizedTests) {
            tests = parameterizedTests.toArray(new Description[0]);
            parameterizedTests.clear();
        }
        Set<TestRecord> records = createRecords(tests, failingTests);
        synchronized (WRITER) {
            try {
                WRITER.appendAll(TestRecord.toCsvRows(records));
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    private static Set<TestRecord> createRecords(Description[] tests, Description[] failingTests) {
        Set<String> ambiguousClassNames = findAmbiguousTestClassNames(tests);
        Set<String> failingTestNames = new HashSet<>();
        for (Description test : failingTests) {
            failingTestNames.add(test.getTestClass().getName() + "#" + getMethodName(test));
        }
        Set<TestRecord> records = new HashSet<>();
        for (Description test : tests) {
            String testClassName = test.getTestClass().getName();
            String testMethodName = getMethodName(test);
            String runnerClassName = test.getTestClass().getAnnotation(RunWith.class).value().getName();
            boolean ambiguous = isAmbiguous(ambiguousClassNames, test, testMethodName);
            boolean failed = failingTestNames.contains(testClassName + "#" + testMethodName);
            records.add(new TestRecord(testClassName, testMethodName, runnerClassName, !ambiguous, !failed));
        }
        return records;
    }

    private static boolean isAmbiguous(Set<String> ambiguousClassNames, Description test, String testMethodName) {
        // All tests in a class are ambiguous if there is one than one test class with that name
        // A test method is ambiguous is there is more than one possible test method with the same name as the test
        // in its test class
        return ambiguousClassNames.contains(test.getTestClass().getName()) ||
                findMethod(test.getTestClass(), testMethodName).size() > 1;
    }

    private static Set<String> findAmbiguousTestClassNames(Description[] tests) {
        Map<String, Set<Class<?>>> classMap = new HashMap<>();
        for (Description test : tests) {
            String key = test.getTestClass().getName();
            if (!classMap.containsKey(key)) {
                classMap.put(key, new HashSet<>());
            }
            classMap.get(key).add(test.getTestClass());
        }
        Set<String> result = new HashSet<>();
        for (String key : classMap.keySet()) {
            if (classMap.get(key).size() > 1) {
                result.add(key);
            }
        }
        return result;
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
