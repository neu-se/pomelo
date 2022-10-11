package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.meringue.ProcessUtil;
import edu.neu.ccs.prl.pomelo.scan.*;
import edu.neu.ccs.prl.pomelo.util.AppendingWriter;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class TestScanner {
    private final SurefireMojoWrapper wrapper;
    private final MojoExecution execution;
    private final File outputDir;
    private final TestPlugin plugin;
    private final Duration timeout;
    private final AppendingWriter writer;

    public TestScanner(SurefireMojoWrapper wrapper, MojoExecution execution, File scanReport, File outputDir,
                       TestPlugin plugin, Duration timeout) throws MojoExecutionException {
        this.wrapper = wrapper;
        this.execution = execution;
        this.outputDir = outputDir;
        this.plugin = plugin;
        this.timeout = timeout;
        try {
            this.writer = new AppendingWriter(scanReport);
        } catch (IOException e) {
            throw new MojoExecutionException("Invalid scan report file:" + scanReport, e);
        }
    }

    public void scan() throws MojoExecutionException, MojoFailureException {
        File tempDir = PluginUtil.createEmptyDirectory(new File(outputDir, "temp"));
        List<TestRecord> records = performInitialScan(tempDir);
        TestLauncher launcher = new TestLauncher(wrapper, tempDir);
        for (TestRecord record : records) {
            writeReportEntry(processRecord(tempDir, record, launcher));
        }
    }

    private void writeReportEntry(ReportEntry entry) throws MojoExecutionException {
        try {
            writer.append(entry.toCsvRow());
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write scan report entries", e);
        }
    }

    private List<TestRecord> performInitialScan(File tempDir) throws MojoExecutionException, MojoFailureException {
        File report = PluginUtil.ensureNew(new File(tempDir, "report.txt"));
        wrapper.getProperties().put("listener", PomeloRunListener.class.getName());
        Properties systemProperties = wrapper.getSystemProperties();
        systemProperties.put("pomelo.listener.report", report.getAbsolutePath());
        wrapper.execute();
        return readRecords(report);
    }

    private ReportEntry processRecord(File tempDir, TestRecord record, TestLauncher launcher)
            throws MojoExecutionException {
        ReportEntry entry = new ReportEntry(wrapper.getProject().getId(), plugin, execution.getExecutionId(), record);
        if (record.passed() && record.isUnambiguous()) {
            return performIsolatedRun(launcher, record, tempDir, entry);
        } else {
            return entry;
        }
    }

    private ReportEntry performIsolatedRun(TestLauncher launcher, TestRecord record, File tempDir, ReportEntry entry)
            throws MojoExecutionException {
        File report = PluginUtil.ensureNew(new File(tempDir, "report.txt"));
        Process process = launcher.launchScanFork(record.getTestClassName(), record.getTestMethodName(), report);
        if (!waitForIsolatedRun(process)) {
            return entry.withIsolatedResult(TestResult.TIMED_OUT);
        }
        if (process.exitValue() != 0) {
            return entry.withIsolatedResult(TestResult.ERROR);
        }
        List<String> lines = PluginUtil.readLines(report);
        return entry.withGeneratorsStatus(GeneratorsStatus.valueOf(lines.get(0).trim()))
                    .withIsolatedResult(TestResult.valueOf(lines.get(1).trim()));
    }

    private boolean waitForIsolatedRun(Process process) {
        try {
            if (timeout != null) {
                return ProcessUtil.waitFor(process, timeout.toMillis(), TimeUnit.MILLISECONDS);
            } else {
                ProcessUtil.waitFor(process);
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static List<TestRecord> readRecords(File file) throws MojoExecutionException {
        try {
            return TestRecord.readCsvRows(file);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read scan records", e);
        }
    }
}
