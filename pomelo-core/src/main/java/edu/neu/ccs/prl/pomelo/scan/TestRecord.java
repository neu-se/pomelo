package edu.neu.ccs.prl.pomelo.scan;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class TestRecord {
    private final String testClassName;
    private final String testMethodName;
    private final String runnerClassName;
    private final boolean unambiguous;
    private final boolean passed;

    public TestRecord(String testClassName, String testMethodName, String runnerClassName, boolean unambiguous,
                      boolean passed) {
        if (testClassName == null || testMethodName == null || runnerClassName == null) {
            throw new NullPointerException();
        }
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
        this.runnerClassName = runnerClassName;
        this.unambiguous = unambiguous;
        this.passed = passed;
    }

    public String getTestClassName() {
        return testClassName;
    }

    public String getTestMethodName() {
        return testMethodName;
    }

    public String getRunnerClassName() {
        return runnerClassName;
    }

    public boolean isUnambiguous() {
        return unambiguous;
    }

    public boolean passed() {
        return passed;
    }

    @Override
    public int hashCode() {
        int result = testClassName.hashCode();
        result = 31 * result + testMethodName.hashCode();
        result = 31 * result + runnerClassName.hashCode();
        result = 31 * result + (unambiguous ? 1 : 0);
        result = 31 * result + (passed ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof TestRecord)) {
            return false;
        }
        TestRecord that = (TestRecord) o;
        if (unambiguous != that.unambiguous) {
            return false;
        }
        if (passed != that.passed) {
            return false;
        }
        if (!testClassName.equals(that.testClassName)) {
            return false;
        }
        if (!testMethodName.equals(that.testMethodName)) {
            return false;
        }
        return runnerClassName.equals(that.runnerClassName);
    }

    @Override
    public String toString() {
        return "TestRecord{" + toCsvRow() + '}';
    }

    public String toCsvRow() {
        return String.format("%s,%s,%s,%s,%s", testClassName, testMethodName, runnerClassName, unambiguous, passed);
    }

    public static List<String> toCsvRows(Collection<TestRecord> records) {
        return records.stream().map(TestRecord::toCsvRow).collect(Collectors.toList());
    }

    public static List<TestRecord> readCsvRows(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        List<TestRecord> records = new ArrayList<>(lines.size());
        for (String line : lines) {
            String[] split = line.split(",");
            TestRecord record = new TestRecord(split[0], split[1], split[2], Boolean.parseBoolean(split[3]),
                                               Boolean.parseBoolean(split[4]));
            records.add(record);
        }
        return records;
    }
}
