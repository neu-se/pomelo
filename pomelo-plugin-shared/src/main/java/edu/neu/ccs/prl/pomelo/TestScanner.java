package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.pomelo.scan.PomeloJUnitListener;
import edu.neu.ccs.prl.pomelo.scan.TestRecord;
import edu.neu.ccs.prl.pomelo.scan.ReportEntry;
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
        writeReportEntries(processRecords(performInitialScan()));
    }

    private void writeReportEntries(List<ReportEntry> entries) throws MojoExecutionException {
        try {
            new AppendingWriter(scanReport).appendAll(ReportEntry.toCsvRows(entries));
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write scan report entries", e);
        }
    }

    private List<TestRecord> performInitialScan() throws MojoExecutionException, MojoFailureException {
        File initialReport = SurefireMojoWrapper.ensureNew(new File(outputDir, "temp-scan.txt"));
        wrapper.getProperties().put("listener", PomeloJUnitListener.class.getName());
        Properties systemProperties = wrapper.getSystemProperties();
        systemProperties.put("pomelo.scan.report", initialReport.getAbsolutePath());
        wrapper.execute();
        return readRecords(initialReport);
    }

    private List<ReportEntry> processRecords(List<TestRecord> records) {
        List<ReportEntry> result = new ArrayList<>(records.size());
        for (TestRecord record : records) {
            result.add(new ReportEntry(wrapper.getProject().getId(), pluginName, execution.getExecutionId(),
                                       record.getTestClassName(), record.getTestMethodName(),
                                       record.getRunnerClassName(), record.isUnambiguous(),
                                       record.passed() ? TestResult.PASSED : TestResult.FAILED,
                                       record.passed() && record.isUnambiguous() ? performIsolatedRun(record) :
                                               TestResult.NONE));
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

    private TestResult performIsolatedRun(TestRecord record) {
        // TODO
        return TestResult.NONE;
    }
}
