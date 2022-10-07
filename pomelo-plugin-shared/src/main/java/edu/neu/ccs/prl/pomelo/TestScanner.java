package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.meringue.ProcessUtil;
import edu.neu.ccs.prl.pomelo.scan.PomeloJUnitListener;
import edu.neu.ccs.prl.pomelo.scan.ReportEntry;
import edu.neu.ccs.prl.pomelo.scan.TestRecord;
import edu.neu.ccs.prl.pomelo.scan.TestResult;
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
        File initialReport = PluginUtil.ensureNew(new File(tempDir, "temp-scan.txt"));
        wrapper.getProperties().put("listener", PomeloJUnitListener.class.getName());
        Properties systemProperties = wrapper.getSystemProperties();
        systemProperties.put("pomelo.scan.report", initialReport.getAbsolutePath());
        wrapper.execute();
        return readRecords(initialReport);
    }

    private List<ReportEntry> processRecords(File tempDir, List<TestRecord> records) throws MojoExecutionException {
        TestLauncher launcher = TestLauncher.create(wrapper, tempDir);
        List<ReportEntry> result = new ArrayList<>(records.size());
        for (TestRecord record : records) {
            result.add(new ReportEntry(wrapper.getProject().getId(), pluginName, execution.getExecutionId(),
                                       record.getTestClassName(), record.getTestMethodName(),
                                       record.getRunnerClassName(), record.isUnambiguous(),
                                       record.passed() ? TestResult.PASSED : TestResult.FAILED,
                                       record.passed() && record.isUnambiguous() ?
                                               performIsolatedRun(launcher, record, tempDir) : TestResult.NONE));
        }
        return result;
    }

    private List<TestRecord> readRecords(File file) throws MojoExecutionException {
        try {
            return TestRecord.readCsvRows(file);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read scan records", e);
        }
    }

    private TestResult performIsolatedRun(TestLauncher launcher, TestRecord record, File tempDir)
            throws MojoExecutionException {
        File report = PluginUtil.ensureNew(new File(tempDir, "temp-scan.txt"));
        Process process = launcher.launchScan(record, report);
        try {
            if (ProcessUtil.waitFor(process) != 0) {
                return TestResult.ERROR;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return TestResult.ERROR;
        }
        List<TestRecord> records = readRecords(report);
        if (records.size() == 1) {
            if (records.get(0).equals(record)) {
                return TestResult.PASSED;
            } else {
                return TestResult.FAILED;
            }
        } else {
            return TestResult.ERROR;
        }
    }
}
