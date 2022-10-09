package edu.neu.ccs.prl.pomelo.scan;

import com.pholser.junit.quickcheck.internal.ParameterTypeContext;
import edu.neu.ccs.prl.pomelo.fuzz.ArgumentsGenerator;
import edu.neu.ccs.prl.pomelo.param.*;
import edu.neu.ccs.prl.pomelo.util.SystemPropertyUtil;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

public final class ScanForkMain {
    private ScanForkMain() {
        throw new AssertionError();
    }

    public static void main(String[] args) throws Throwable {
        Class<?> testClass = Class.forName(args[0], true, ScanForkMain.class.getClassLoader());
        String testMethodName = args[1];
        ParameterizedTestWrapper wrapper = ParameterizedTestType.wrap(new TestMethod(testClass, testMethodName));
        SystemPropertyUtil.load(new File(args[2]));
        File report = new File(args[3]);
        GeneratorsStatus status = checkGeneratorStatus(wrapper);
        TestResult result = getTestResult(testClass, testMethodName, wrapper);
        try (PrintWriter out = new PrintWriter(report)) {
            out.println(status);
            out.println(result);
        }
    }

    private static GeneratorsStatus checkGeneratorStatus(ParameterizedTestWrapper wrapper) {
        List<ParameterTypeContext> contexts = wrapper.getParameterTypeContexts();
        if (contexts.stream().allMatch(ArgumentsGenerator::generatorAvailable)) {
            return GeneratorsStatus.PRESENT;
        } else {
            return GeneratorsStatus.MISSING;
        }
    }

    private static TestResult getTestResult(Class<?> testClass, String testMethodName,
                                            ParameterizedTestWrapper wrapper) {
        try {
            ParameterSupplier supplier = new ListParameterSupplier(wrapper.getOriginalParameterGroups());
            ParameterizedRunner runner = wrapper.createParameterizedRunner(supplier);
            RunNotifier notifier = new RunNotifier();
            IsolatedScanRunListener listener = new IsolatedScanRunListener(testClass, testMethodName);
            notifier.addListener(listener);
            runner.run(notifier);
            if (listener.nonMatchFinished || !listener.matchFinished) {
                return TestResult.ERROR;
            } else if (listener.failed) {
                return TestResult.FAILED;
            } else {
                return TestResult.PASSED;
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return TestResult.ERROR;
        }
    }

    private static class IsolatedScanRunListener extends RunListener {
        private final Class<?> testClass;
        private final String testMethodName;
        private boolean nonMatchFinished = false;
        private boolean failed = false;
        private boolean matchFinished = false;

        public IsolatedScanRunListener(Class<?> testClass, String testMethodName) {
            this.testClass = testClass;
            this.testMethodName = testMethodName;
        }

        @Override
        public void testFinished(Description d) {
            if (matchesTarget(d)) {
                matchFinished = true;
            } else if (d.isTest() && d.getTestClass() != null && d.getMethodName() != null) {
                nonMatchFinished = true;
            }
        }

        @Override
        public void testFailure(Failure failure) {
            failed |= matchesTarget(failure.getDescription());
        }

        private boolean matchesTarget(Description d) {
            return d.getMethodName() != null && d.getTestClass() != null
                    && testClass.equals(d.getTestClass())
                    && testMethodName.equals(TestMethod.getMethodName(d.getMethodName()));
        }
    }
}
