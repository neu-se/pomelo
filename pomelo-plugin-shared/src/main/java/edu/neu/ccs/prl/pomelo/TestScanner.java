package edu.neu.ccs.prl.pomelo;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;
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
        File initialReport = SurefireMojoWrapper.ensureNew(new File(outputDir, "temp-scan.txt"));
        addScanListener(initialReport);
        wrapper.execute();
        try {
            List<TestRecord> records = TestRecord.readCsvRows(initialReport);
            for (TestRecord record : records) {
                record.setExecution(execution.getExecutionId());
                record.setPlugin(pluginName);
                record.setProject(wrapper.getProject().getId());
            }
            new EntryWriter(scanReport).appendAll(TestRecord.toCsvRows(records));
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to process scan files", e);
        }
    }

    public void addScanListener(File report) throws MojoExecutionException {
        wrapper.getProperties().put("listener", PomeloJUnitListener.class.getName());
        Properties systemProperties = wrapper.getSystemProperties();
        systemProperties.put("pomelo.scan.report", report.getAbsolutePath());
    }
}
