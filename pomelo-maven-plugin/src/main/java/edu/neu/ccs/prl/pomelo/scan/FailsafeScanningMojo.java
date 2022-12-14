package edu.neu.ccs.prl.pomelo.scan;

import org.apache.maven.artifact.resolver.ResolutionErrorHandler;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.failsafe.IntegrationTestMojo;
import org.apache.maven.plugins.annotations.*;

import java.io.File;

@Mojo(name = "scan-integration-test", requiresDependencyResolution = ResolutionScope.TEST,
        defaultPhase = LifecyclePhase.INTEGRATION_TEST)
public class FailsafeScanningMojo extends IntegrationTestMojo {
    /**
     * The current execution of this plugin.
     */
    @Parameter(defaultValue = "${mojoExecution}", readonly = true, required = true)
    private MojoExecution mojoExecution;
    /**
     * File to which pomelo report entries should be written.
     */
    @Parameter(property = "pomelo.report.internal", required = true)
    private File report;
    /**
     * Directory used to store internal temporary files created by Pomelo.
     */
    @Parameter(defaultValue = "${project.build.directory}/pomelo/scan", readonly = true, required = true)
    private File temporaryDirectory;
    /**
     * Amount of time in seconds after which forked isolated test JVMs should be killed. If set to 0, forked isolated
     * test JVMs should never be timed out.
     */
    @Parameter(property = "pomelo.timeout", defaultValue = "0")
    private int timeout;
    /**
     * True if the standard output and error of the forked isolated test JVMs should be redirected to the standard out
     * and error of the Maven process. Otherwise, the standard output and error of the forked isolated test JVMs is
     * discarded.
     */
    @Parameter(property = "pomelo.verbose", defaultValue = "false")
    private boolean verbose;
    @Component
    private ResolutionErrorHandler errorHandler;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        new PomeloScanner(this, timeout, errorHandler, temporaryDirectory, verbose, TestPluginType.FAILSAFE,
                          mojoExecution, report, super::execute).scan();
    }
}