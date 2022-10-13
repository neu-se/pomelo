package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.meringue.ProcessUtil;
import edu.neu.ccs.prl.pomelo.scan.*;
import edu.neu.ccs.prl.pomelo.util.AppendingWriter;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

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
    private final boolean quiet;
    private final Log log;
    private final AppendingWriter writer;

    public TestScanner(SurefireMojoWrapper wrapper, MojoExecution execution, File scanReport, File outputDir,
                       TestPlugin plugin, Duration timeout, boolean quiet, Log log) throws MojoExecutionException {
        this.wrapper = wrapper;
        this.execution = execution;
        this.outputDir = outputDir;
        this.plugin = plugin;
        this.timeout = timeout;
        this.quiet = quiet;
        this.log = log;
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
            throw new MojoExecutionException("Failed to write scan report entry", e);
        }
    }

    private List<TestRecord> performInitialScan(File tempDir) throws MojoExecutionException, MojoFailureException {
        log.info(String.format("Executing tests with %s", plugin));
        File report = PluginUtil.ensureNew(new File(tempDir, "report.txt"));
        wrapper.getProperties().put("listener", PomeloRunListener.class.getName());
        Properties systemProperties = wrapper.getSystemProperties();
        systemProperties.put("pomelo.listener.report", report.getAbsolutePath());
        wrapper.execute();
        log.info("> Finished executing tests");
        return readRecords(report);
    }

    private ReportEntry processRecord(File tempDir, TestRecord record, TestLauncher launcher)
            throws MojoExecutionException {
        ReportEntry entry = new ReportEntry(wrapper.getProject().getId(), plugin, execution.getExecutionId(), record);
        if (record.passed() && record.isUnambiguous()) {
            return performIsolatedRun(launcher, record, tempDir, entry);
        } else {
            log.info(String.format("> Skipping isolated run for %s", record.getTestDescription()));
            return entry;
        }
    }

    private ReportEntry performIsolatedRun(TestLauncher launcher, TestRecord record, File tempDir, ReportEntry entry)
            throws MojoExecutionException {
        log.info(String.format("Starting isolated run for %s", record.getTestDescription()));
        File report = PluginUtil.ensureNew(new File(tempDir, "report.txt"));
        Process process = launcher.launchScanFork(record.getTestClassName(), record.getTestMethodName(), report, quiet);
        if (!waitForIsolatedRun(process)) {
            log.info(String.format("> Isolated run for %s timed out", record.getTestDescription()));
            return entry.withIsolatedResult(TestResult.TIMED_OUT);
        }
        if (process.exitValue() != 0) {
            log.info(String.format("> Isolated run for %s produced an error", record.getTestDescription()));
            return entry.withIsolatedResult(TestResult.ERROR);
        }
        log.info(String.format("> Finished isolated run for %s", record.getTestDescription()));
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

    private List<TestRecord> readRecords(File file) throws MojoExecutionException {
        log.info("Reading initial Pomelo scan records");
        try {
            List<TestRecord> records = TestRecord.readCsvRows(file);
            log.info(String.format("> Read %d initial Pomelo scan records", records.size()));
            return records;
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read initial Pomelo scan records", e);
        }
    }
}
