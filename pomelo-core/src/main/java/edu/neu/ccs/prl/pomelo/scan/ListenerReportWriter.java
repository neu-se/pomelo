package edu.neu.ccs.prl.pomelo.scan;

import edu.neu.ccs.prl.pomelo.param.ParameterizedTestType;
import edu.neu.ccs.prl.pomelo.util.AppendingWriter;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ListenerReportWriter {
    private final Set<TestMethod> failingTests = new HashSet<>();
    private final Set<TestMethod> tests = new HashSet<>();
    private final AppendingWriter writer;

    public ListenerReportWriter(File file) {
        this.writer = new AppendingWriter(file);
        Runtime.getRuntime().addShutdownHook(new Thread(this::writeReport));
    }

    public synchronized void markTestAsFailing(TestMethod test) {
        if (ParameterizedTestType.isParameterized(test)) {
            failingTests.add(test);
        }
    }

    public synchronized void addTest(TestMethod test) {
        if (ParameterizedTestType.isParameterized(test)) {
            tests.add(test);
        }
    }

    public synchronized void writeReport() {
        Set<TestRecord> records = createRecords();
        synchronized (PomeloRunListener.class) {
            try {
                writer.appendAll(TestRecord.toCsvRows(records));
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        failingTests.clear();
        tests.clear();
    }

    private Set<TestRecord> createRecords() {
        Set<TestMethod> ambiguousTests = computeAmbiguousTests(tests);
        Set<TestRecord> records = new HashSet<>();
        for (TestMethod test : tests) {
            records.add(new TestRecord(test.getTestClass().getName(), test.getTestMethodName(),
                                       test.getTestClass().getAnnotation(RunWith.class).value().getName(),
                                       !ambiguousTests.contains(test), !failingTests.contains(test)));
        }
        return records;
    }

    private static Set<TestMethod> computeAmbiguousTests(Iterable<TestMethod> tests) {
        Map<String, Set<Class<?>>> classMap = new HashMap<>();
        for (TestMethod test : tests) {
            String key = test.getTestClass().getName();
            if (!classMap.containsKey(key)) {
                classMap.put(key, new HashSet<>());
            }
            classMap.get(key).add(test.getTestClass());
        }
        Set<TestMethod> result = new HashSet<>();
        for (TestMethod test : tests) {
            if (classMap.get(test.getTestClass().getName()).size() > 1 || test.findMethod(Test.class).size() != 1) {
                result.add(test);
            }
        }
        return result;
    }
}
