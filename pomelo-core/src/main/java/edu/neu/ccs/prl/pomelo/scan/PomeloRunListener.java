package edu.neu.ccs.prl.pomelo.scan;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunListener.ThreadSafe;

import java.io.File;

@ThreadSafe
public class PomeloRunListener extends RunListener {
    private final ListenerReportWriter writer;

    @SuppressWarnings("unused")
    public PomeloRunListener() {
        String path = System.getProperty("pomelo.listener.report");
        if (path != null) {
            this.writer = new ListenerReportWriter(new File(path));
            Runtime.getRuntime().addShutdownHook(new Thread(writer::writeReport));
        } else {
            writer = null;
        }
    }

    @Override
    public void testFinished(Description description) {
        if (writer != null && description.isTest() && description.getTestClass() != null &&
                description.getMethodName() != null) {
            writer.addTest(
                    new TestMethod(description.getTestClass(), TestMethod.getMethodName(description.getMethodName())));
        }
    }

    @Override
    public void testFailure(Failure failure) {
        Description description = failure.getDescription();
        if (writer != null && description.isTest() && description.getTestClass() != null &&
                description.getMethodName() != null) {
            writer.markTestAsFailing(
                    new TestMethod(description.getTestClass(), TestMethod.getMethodName(description.getMethodName())));
        }
    }
}
