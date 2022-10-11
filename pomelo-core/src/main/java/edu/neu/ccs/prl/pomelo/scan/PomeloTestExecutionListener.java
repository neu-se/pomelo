package edu.neu.ccs.prl.pomelo.scan;

import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@SuppressWarnings("unused")
public class PomeloTestExecutionListener implements TestExecutionListener {
    private final ListenerReportWriter writer;
    private TestPlan plan;

    @SuppressWarnings("unused")
    public PomeloTestExecutionListener() throws IOException {
        String path = System.getProperty("pomelo.listener.report");
        if (path != null) {
            this.writer = new ListenerReportWriter(new File(path));
            Runtime.getRuntime().addShutdownHook(new Thread(writer::writeReport));
        } else {
            writer = null;
        }
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        this.plan = testPlan;
    }

    @Override
    public void executionFinished(TestIdentifier identifier, TestExecutionResult result) {
        if (writer != null && identifier.isTest()) {
            Class<?> testClass = getTestClass(plan, identifier);
            if (testClass != null) {
                TestMethod method = new TestMethod(testClass, TestMethod.getMethodName(identifier.getDisplayName()));
                writer.addTest(method);
                if (result.getStatus() == TestExecutionResult.Status.FAILED) {
                    writer.markTestAsFailing(method);
                }
            }
        }
    }

    private static Class<?> getTestClass(TestPlan plan, TestIdentifier identifier) {
        try {
            ClassSource source = getClassSource(plan, identifier);
            return source == null ? null : source.getJavaClass();
        } catch (PreconditionViolationException e) {
            return null;
        }
    }

    private static ClassSource getClassSource(TestPlan plan, TestIdentifier identifier) {
        for (TestIdentifier current = identifier; current != null; current = plan.getParent(current).orElse(null)) {
            Optional<ClassSource> source =
                    current.getSource().filter(ClassSource.class::isInstance).map(ClassSource.class::cast);
            if (source.isPresent()) {
                return source.get();
            }
        }
        return null;
    }
}
