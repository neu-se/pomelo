package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.meringue.JvmLauncher;
import edu.neu.ccs.prl.meringue.ProcessUtil;
import edu.neu.ccs.prl.pomelo.scan.*;
import edu.neu.ccs.prl.pomelo.util.AppendingWriter;
import org.apache.maven.artifact.resolver.ResolutionErrorHandler;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.surefire.AbstractSurefireMojo;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PomeloScanner {
    private final AbstractSurefireMojo mojo;
    private final AppendingWriter writer;
    private final DependencyResolver resolver;
    private final int timeout;
    private final MojoExecutor executor;
    private final File temporaryDirectory;
    private final boolean verbose;
    private final MojoExecution mojoExecution;
    private final TestPluginType pluginType;

    public PomeloScanner(AbstractSurefireMojo mojo, int timeout, ResolutionErrorHandler errorHandler,
                         File temporaryDirectory, boolean verbose, TestPluginType pluginType, MojoExecution mojoExecution,
                         File report, MojoExecutor executor) throws MojoExecutionException {
        this.mojo = mojo;
        this.timeout = timeout;
        this.executor = executor;
        this.resolver = new DependencyResolver(mojo.getRepositorySystem(), mojo.getLocalRepository(),
                                               mojo.getRemoteRepositories(), errorHandler,
                                               mojo.getSession().isOffline());
        this.temporaryDirectory = temporaryDirectory;
        this.verbose = verbose;
        this.pluginType = pluginType;
        this.mojoExecution = mojoExecution;
        try {
            this.writer = new AppendingWriter(report);
        } catch (IOException e) {
            throw new MojoExecutionException("Invalid scan report file", e);
        }
    }

    public void scan() throws MojoExecutionException, MojoFailureException {
        PluginUtil.ensureEmptyDirectory(temporaryDirectory);
        addPomeloCoreToClasspath();
        List<TestRecord> records = performInitialScan();
        JvmLauncher launcher = createLauncher();
        for (TestRecord record : records) {
            writeReportEntry(processRecord(record, launcher));
        }
    }

    private void addPomeloCoreToClasspath() throws MojoExecutionException {
        String[] originalElements = mojo.getAdditionalClasspathElements();
        List<String> elements =
                originalElements == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(originalElements));
        getCoreArtifactClasspath().stream().map(File::getAbsolutePath).forEach(elements::add);
        mojo.setAdditionalClasspathElements(elements.toArray(elements.toArray(new String[0])));
    }

    private JvmLauncher createLauncher() throws MojoExecutionException {
        JvmConfiguration config = new SurefireMojoWrapper(mojo).extractJvmConfiguration();
        return config.createLauncher(temporaryDirectory, 1, verbose)
                     .withArguments(config.writeSystemProperties(temporaryDirectory, 1).getAbsolutePath());
    }

    private void writeReportEntry(ReportEntry entry) throws MojoExecutionException {
        try {
            writer.append(entry.toCsvRow());
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write scan report entry", e);
        }
    }

    private List<TestRecord> performInitialScan() throws MojoExecutionException, MojoFailureException {
        SurefireMojoWrapper wrapper = new SurefireMojoWrapper(mojo);
        mojo.getLog().info(String.format("Executing tests with %s", pluginType));
        File initial = PluginUtil.ensureNew(new File(temporaryDirectory, "initial.txt"));
        wrapper.getProperties().put("listener", PomeloRunListener.class.getName());
        Properties systemProperties = wrapper.getSystemProperties();
        systemProperties.put("pomelo.listener.report", initial.getAbsolutePath());
        executor.execute();
        mojo.getLog().info("> Finished executing tests");
        return readRecords(initial);
    }

    private ReportEntry processRecord(TestRecord record, JvmLauncher launcher) throws MojoExecutionException {
        ReportEntry entry =
                new ReportEntry(mojo.getProject().getId(), pluginType, mojoExecution.getExecutionId(), record);
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
        File report = PluginUtil.ensureNew(new File(temporaryDirectory, "report.txt"));
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
            if (timeout != 0) {
                return ProcessUtil.waitFor(process, timeout, TimeUnit.SECONDS);
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

    private List<File> getCoreArtifactClasspath() throws MojoExecutionException {
        return resolver.resolve(mojo.getPluginArtifactMap().get("edu.neu.ccs.prl.pomelo:pomelo-core"));
    }

    public interface MojoExecutor {
        void execute() throws MojoExecutionException, MojoFailureException;
    }
}
