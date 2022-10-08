package edu.neu.ccs.prl.pomelo.scan;

import edu.neu.ccs.prl.pomelo.param.ParameterizedTestType;
import edu.neu.ccs.prl.pomelo.util.AppendingWriter;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunListener.ThreadSafe;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@ThreadSafe
public class PomeloJUnitListener extends RunListener {
    private final Set<Description> failingParameterizedTests = new HashSet<>();
    private final Set<Description> parameterizedTests = new HashSet<>();
    private final AppendingWriter writer;

    @SuppressWarnings("unused")
    public PomeloJUnitListener() {
        this(new File(System.getProperty("pomelo.scan.report")));
    }

    public PomeloJUnitListener(File file) {
        this.writer = new AppendingWriter(file);
        Runtime.getRuntime().addShutdownHook(new Thread(this::writeTestRecords));
    }

    @Override
    public synchronized void testFinished(Description description) {
        if (isParameterizedTest(description)) {
            parameterizedTests.add(description);
        }
    }

    @Override
    public synchronized void testFailure(Failure failure) {
        Description description = failure.getDescription();
        if (isParameterizedTest(description)) {
            failingParameterizedTests.add(description);
        }
    }

    public synchronized void writeTestRecords() {
        Set<TestRecord> records = createRecords();
        synchronized (PomeloJUnitListener.class) {
            try {
                writer.appendAll(TestRecord.toCsvRows(records));
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        parameterizedTests.clear();
        failingParameterizedTests.clear();
    }

    private Set<TestRecord> createRecords() {
        Set<String> ambiguousClassNames = findAmbiguousTestClassNames(parameterizedTests);
        Set<String> failingTestNames = new HashSet<>();
        for (Description test : failingParameterizedTests) {
            failingTestNames.add(test.getTestClass().getName() + "#" + getMethodName(test));
        }
        Set<TestRecord> records = new HashSet<>();
        for (Description test : parameterizedTests) {
            String testClassName = test.getTestClass().getName();
            String testMethodName = getMethodName(test);
            String runnerClassName = test.getTestClass().getAnnotation(RunWith.class).value().getName();
            boolean unambiguous = !ambiguousClassNames.contains(test.getTestClass().getName()) &&
                    findMethod(test.getTestClass(), testMethodName).size() == 1;
            boolean succeeded = !failingTestNames.contains(testClassName + "#" + testMethodName);
            records.add(new TestRecord(testClassName, testMethodName, runnerClassName, unambiguous, succeeded));
        }
        return records;
    }

    private static Set<String> findAmbiguousTestClassNames(Iterable<Description> tests) {
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

    static String getMethodName(Description description) {
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

    private static boolean isParameterizedTest(Description d) {
        return d.getTestClass() != null && d.getMethodName() != null &&
                ParameterizedTestType.isParameterizedTest(d.getTestClass());
    }
}
