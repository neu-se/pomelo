package edu.neu.ccs.prl.pomelo;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.surefire.SurefirePlugin;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;

@Mojo(name = "scan-test", defaultPhase = LifecyclePhase.TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class SurefireScanningMojo extends SurefirePlugin {
    private final SurefireMojoWrapper wrapper = new SurefireMojoWrapper(this, super::execute);
    /**
     * The current execution of this plugin.
     */
    @Parameter(defaultValue = "${mojoExecution}", readonly = true, required = true)
    private MojoExecution mojoExecution;
    /**
     * File to which pomelo report entries are written.
     */
    @Parameter(required = true)
    private File scanReport;
    /**
     * Directory to which output files should be written.
     */
    @Parameter(defaultValue = "${project.build.directory}/pomelo", readonly = true, required = true)
    private File outputDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        new TestScanner(wrapper, mojoExecution, scanReport, outputDir,"surefire").scan();
    }
}