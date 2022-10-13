package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.pomelo.scan.TestPlugin;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.failsafe.IntegrationTestMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.time.Duration;

@Mojo(name = "scan-integration-test", requiresDependencyResolution = ResolutionScope.TEST,
        defaultPhase = LifecyclePhase.INTEGRATION_TEST)
public class FailsafeScanningMojo extends IntegrationTestMojo {
    private final SurefireMojoWrapper wrapper = new SurefireMojoWrapper(this, super::execute);
    /**
     * The current execution of this plugin.
     */
    @Parameter(defaultValue = "${mojoExecution}", readonly = true, required = true)
    private MojoExecution mojoExecution;
    /**
     * File to which pomelo report entries are written.
     */
    @Parameter(property = "pomelo.scan.report.absolute", required = true)
    private File scanReport;
    /**
     * Directory to which output files should be written.
     */
    @Parameter(defaultValue = "${project.build.directory}/pomelo/scan", readonly = true, required = true)
    private File outputDir;
    /**
     * Amount of time in seconds after which forked isolated test JVMs should be killed. If set to 0, forked isolated
     * test JVMs should never be timed out.
     */
    @Parameter(property = "pomelo.scan.timeout", defaultValue = "0")
    private int scanTimeout;
    /**
     * True if the standard output and error of the forked isolated test JVMs should be discarded. By default, the
     * standard output and error of the forked isolated test JVMs is redirected to the standard out and error of the
     * Maven process.
     */
    @Parameter(property = "pomelo.quiet", defaultValue = "false")
    private boolean quiet;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Duration timeout = scanTimeout == 0 ? null : Duration.ofSeconds(scanTimeout);
        new TestScanner(wrapper, mojoExecution, scanReport, outputDir, TestPlugin.FAILSAFE, timeout, quiet, getLog())
                .scan();
    }
}