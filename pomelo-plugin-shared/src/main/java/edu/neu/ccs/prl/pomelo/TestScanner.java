package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.meringue.JvmLauncher;
import edu.neu.ccs.prl.meringue.ProcessUtil;
import edu.neu.ccs.prl.pomelo.scan.*;
import edu.neu.ccs.prl.pomelo.util.AppendingWriter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.surefire.AbstractSurefireMojo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class TestScanner {
    private final SurefireMojoWrapper wrapper;
    private final ScanningMojo mojo;
    private final AppendingWriter writer;

    public TestScanner(ScanningMojo mojo) throws MojoExecutionException {
        this.wrapper = new SurefireMojoWrapper((AbstractSurefireMojo) mojo);
        this.mojo = mojo;
        try {
            this.writer = new AppendingWriter(mojo.getReport());
        } catch (IOException e) {
            throw new MojoExecutionException("Invalid scan report file", e);
        }
    }

    public void scan() throws MojoExecutionException, MojoFailureException {
        PluginUtil.createEmptyDirectory(mojo.getTemporaryDirectory());
        addPomeloCoreToClasspath();
        List<TestRecord> records = performInitialScan();
        JvmLauncher launcher = createLauncher();
        for (TestRecord record : records) {
            writeReportEntry(processRecord(record, launcher));
        }
    }

    private void addPomeloCoreToClasspath() throws MojoExecutionException {
        AbstractSurefireMojo m = (AbstractSurefireMojo) mojo;
        String[] originalElements = m.getAdditionalClasspathElements();
        List<String> elements =
                originalElements == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(originalElements));
        mojo.getCoreArtifactClasspath().stream().map(File::getAbsolutePath).forEach(elements::add);
        m.setAdditionalClasspathElements(elements.toArray(elements.toArray(new String[0])));
    }

    private JvmLauncher createLauncher() throws MojoExecutionException {
        TestJvmConfiguration config = wrapper.extractTestJvmConfiguration();
        return config.createLauncher(mojo.getTemporaryDirectory(), 0, mojo.getCoreArtifactClasspath(),
                                     mojo.isVerbose())
                     .withArguments(config.writeSystemProperties(mojo.getTemporaryDirectory(), 0)
                                          .getAbsolutePath());
    }

    private void writeReportEntry(ReportEntry entry) throws MojoExecutionException {
        try {
            writer.append(entry.toCsvRow());
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write scan report entry", e);
        }
    }

    private List<TestRecord> performInitialScan() throws MojoExecutionException, MojoFailureException {
        mojo.getLog().info(String.format("Executing tests with %s", mojo.getOriginalPluginType()));
        File initial = PluginUtil.ensureNew(new File(mojo.getTemporaryDirectory(), "initial.txt"));
        wrapper.getProperties().put("listener", PomeloRunListener.class.getName());
        Properties systemProperties = wrapper.getSystemProperties();
        systemProperties.put("pomelo.listener.report", initial.getAbsolutePath());
        mojo.executeSuper();
        mojo.getLog().info("> Finished executing tests");
        return readRecords(initial);
    }

    private ReportEntry processRecord(TestRecord record, JvmLauncher launcher) throws MojoExecutionException {
        ReportEntry entry = new ReportEntry(wrapper.getProject().getId(), mojo.getOriginalPluginType(),
                                            mojo.getMojoExecution().getExecutionId(), record);
        if (record.passed() && record.isUnambiguous()) {
            return performIsolatedRun(launcher, record, entry);
        } else {
            mojo.getLog().info(String.format("> Skipping isolated run for %s", record.getTestDescription()));
            return entry;
        }
    }

    private ReportEntry performIsolatedRun(JvmLauncher launcher, TestRecord record, ReportEntry entry)
            throws MojoExecutionException {
        mojo.getLog().info(String.format("Starting isolated run for %s", record.getTestDescription()));
        File report = PluginUtil.ensureNew(new File(mojo.getTemporaryDirectory(), "report.txt"));
        launcher = launcher.appendArguments(report.getAbsolutePath(), record.getTestClassName(),
                                            record.getTestMethodName());
        Process process;
        try {
            process = launcher.launch();
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to launch test JVM", e);
        }
        if (!waitForIsolatedRun(process)) {
            mojo.getLog().info(String.format("> Isolated run for %s timed out", record.getTestDescription()));
            return entry.withIsolatedResult(TestResult.TIMED_OUT);
        }
        if (process.exitValue() != 0) {
            mojo.getLog().info(String.format("> Isolated run for %s produced an error", record.getTestDescription()));
            return entry.withIsolatedResult(TestResult.ERROR);
        }
        mojo.getLog().info(String.format("> Finished isolated run for %s", record.getTestDescription()));
        List<String> lines = PluginUtil.readLines(report);
        return entry.withGeneratorsStatus(GeneratorsStatus.valueOf(lines.get(0).trim()))
                    .withIsolatedResult(TestResult.valueOf(lines.get(1).trim()));
    }

    private boolean waitForIsolatedRun(Process process) {
        try {
            if (mojo.getTimeout() != 0) {
                return ProcessUtil.waitFor(process, mojo.getTimeout(), TimeUnit.SECONDS);
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
        mojo.getLog().info("Reading initial Pomelo scan records");
        try {
            List<TestRecord> records = TestRecord.readCsvRows(file);
            mojo.getLog().info(String.format("> Read %d initial Pomelo scan records", records.size()));
            return records;
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read initial Pomelo scan records", e);
        }
    }
}
