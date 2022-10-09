package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.meringue.ProcessUtil;
import edu.neu.ccs.prl.pomelo.scan.*;
import edu.neu.ccs.prl.pomelo.util.AppendingWriter;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class TestScanner {
    private final SurefireMojoWrapper wrapper;
    private final MojoExecution execution;
    private final File scanReport;
    private final File outputDir;
    private final String pluginName;

    public TestScanner(SurefireMojoWrapper wrapper, MojoExecution execution, File scanReport, File outputDir,
                       String pluginName) {
        this.wrapper = wrapper;
        this.execution = execution;
        this.scanReport = scanReport;
        this.outputDir = outputDir;
        this.pluginName = pluginName;
    }

    public void scan() throws MojoExecutionException, MojoFailureException {
        File tempDir = PluginUtil.createEmptyDirectory(new File(outputDir, "temp"));
        writeReportEntries(processRecords(tempDir, performInitialScan(tempDir)));
    }

    private void writeReportEntries(List<ReportEntry> entries) throws MojoExecutionException {
        try {
            new AppendingWriter(scanReport).appendAll(ReportEntry.toCsvRows(entries));
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write scan report entries", e);
        }
    }

    private List<TestRecord> performInitialScan(File tempDir) throws MojoExecutionException, MojoFailureException {
        File report = PluginUtil.ensureNew(new File(tempDir, "report.txt"));
        wrapper.getProperties().put("listener", PomeloJUnitListener.class.getName());
        Properties systemProperties = wrapper.getSystemProperties();
        systemProperties.put("pomelo.listener.report", report.getAbsolutePath());
        wrapper.execute();
        return readRecords(report);
    }

    private List<ReportEntry> processRecords(File tempDir, List<TestRecord> records) throws MojoExecutionException {
        TestLauncher launcher = TestLauncher.create(wrapper, tempDir);
        List<ReportEntry> entries = new ArrayList<>(records.size());
        for (TestRecord record : records) {
            ReportEntry entry = new ReportEntry(wrapper.getProject().getId(), pluginName, execution.getExecutionId(),
                                                record.getTestClassName(), record.getTestMethodName(),
                                                record.getRunnerClassName(), record.isUnambiguous(),
                                                record.passed() ? TestResult.PASSED : TestResult.FAILED,
                                                TestResult.NONE, GeneratorsStatus.UNKNOWN);
            if (record.passed() && record.isUnambiguous()) {
                entry = performIsolatedRun(launcher, record, tempDir, entry);
            }
            entries.add(entry);
        }
        return entries;
    }

    private ReportEntry performIsolatedRun(TestLauncher launcher, TestRecord record, File tempDir, ReportEntry entry)
            throws MojoExecutionException {
        File report = PluginUtil.ensureNew(new File(tempDir, "report.txt"));
        Process process = launcher.launchScanFork(record.getTestClassName(), record.getTestMethodName(), report);
        try {
            if (ProcessUtil.waitFor(process) != 0) {
                return entry.withIsolatedResult(TestResult.ERROR);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return entry.withIsolatedResult(TestResult.ERROR);
        }
        List<String> lines = PluginUtil.readLines(report);
        return entry.withGeneratorsStatus(GeneratorsStatus.valueOf(lines.get(0).trim()))
                    .withIsolatedResult(TestResult.valueOf(lines.get(1).trim()));
    }

    private static List<TestRecord> readRecords(File file) throws MojoExecutionException {
        try {
            return TestRecord.readCsvRows(file);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read scan records", e);
        }
    }
}
