package edu.neu.ccs.prl.pomelo;

import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.SurefirePlugin;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;

import java.io.File;

@Mojo(name = "fuzz-test", defaultPhase = LifecyclePhase.TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class SurefireFuzzingMojo extends SurefirePlugin {
    /**
     * The Maven session.
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;
    /**
     * The current Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    /**
     * Fully-qualified name of the test class.
     *
     * @see Class#forName(String className)
     */
    @Parameter(property = "pomelo.testClass", required = true)
    private String testClass;
    /**
     * Name of the test method.
     */
    @Parameter(property = "pomelo.testMethod", required = true)
    private String testMethod;
    /**
     * Textual representation of the maximum amount of time to execute the fuzzing campaign in the ISO-8601 duration
     * format. The default value is one day.
     * <p>
     * See {@link java.time.Duration#parse(CharSequence)}.
     */
    @Parameter(property = "pomelo.duration", defaultValue = "P1D")
    private String duration;
    /**
     * Directory to which output files should be written.
     */
    @Parameter(property = "pomelo.outputDirectory", defaultValue = "${project.build.directory}/pomelo/out")
    private File outputDirectory;
    /**
     * Maximum number of frames to include in stack traces taken for failures. By default, a maximum of {@code 5} frames
     * are included.
     */
    @Parameter(property = "pomelo.maxTraceSize", defaultValue = "5")
    private int maxTraceSize;
    /**
     * True if forked analysis JVMs should suspend and wait for a debugger to attach.
     */
    @Parameter(property = "pomelo.debug", defaultValue = "false")
    private boolean debug;
    /**
     * Maximum amount of time in seconds to execute a single input during analysis or {@code -1} if no timeout should be
     * used. By default, a timeout value of {@code 600} seconds is used.
     */
    @Parameter(property = "pomelo.timeout", defaultValue = "600")
    private long timeout;
    /**
     * True if the standard output and error of the forked analysis JVMs should be redirected to the standard out and
     * error of the Maven process. Otherwise, the standard output and error of the forked analysis JVMs is discarded.
     */
    @Parameter(property = "pomelo.verbose", defaultValue = "false")
    private boolean verbose;
    /**
     * Directory used to store internal temporary files created by Pomelo.
     */
    @Parameter(defaultValue = "${project.build.directory}/pomelo/fuzz/temp", readonly = true, required = true)
    private File temporaryDirectory;
    @Component
    private ArtifactResolver artifactResolver;
    @Component
    private ArtifactHandlerManager artifactHandlerManager;

    @Override
    public void execute() throws MojoExecutionException {
        new TestFuzzer(this, testClass, testMethod, duration, outputDirectory, maxTraceSize, debug, timeout,
                       verbose, temporaryDirectory, artifactResolver, artifactHandlerManager).execute();
    }
}
