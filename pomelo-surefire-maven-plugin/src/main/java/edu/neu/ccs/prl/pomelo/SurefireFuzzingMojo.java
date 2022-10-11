package edu.neu.ccs.prl.pomelo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.SurefirePlugin;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Mojo(name = "fuzz-test", defaultPhase = LifecyclePhase.TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class SurefireFuzzingMojo extends SurefirePlugin {
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
    @Parameter(property = "pomelo.outputDir", defaultValue = "${project.build.directory}/pomelo")
    private File outputDir;
    /**
     * Fully-qualified name of the fuzzing framework that should be used.
     */
    @Parameter(property = "pomelo.framework", readonly = true, required = true)
    private String framework;
    /**
     * Arguments used to configure the fuzzing framework.
     */
    @Parameter(readonly = true)
    private Properties frameworkArguments = new Properties();
    /**
     * Java command line options that should be used for test JVMs.
     */
    @Parameter(property = "pomelo.javaOptions")
    private List<String> javaOptions = new ArrayList<>();

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info(String.format("Fuzzing %s#%s", testClass, testMethod));
        PluginUtil.createEmptyDirectory(outputDir);
        File tempDir = PluginUtil.createEmptyDirectory(new File(outputDir, "temp"));
        TestLauncher launcher = new TestLauncher(new SurefireMojoWrapper(this, super::execute), tempDir);
        // TODO
    }
}
