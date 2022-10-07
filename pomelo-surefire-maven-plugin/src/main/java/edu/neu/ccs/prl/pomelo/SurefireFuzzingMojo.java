package edu.neu.ccs.prl.pomelo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.SurefirePlugin;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;

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
     * Directory to which output files should be written.
     */
    @Parameter(property = "pomelo.outputDir", defaultValue = "${project.build.directory}/pomelo")
    private File outputDir;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info(String.format("Fuzzing %s#%s", testClass, testMethod));
        PluginUtil.createEmptyDirectory(outputDir);
        File tempDir = PluginUtil.createEmptyDirectory(new File(outputDir, "temp"));
        TestLauncher launcher = TestLauncher.create(new SurefireMojoWrapper(this, super::execute), tempDir);
        // TODO
    }
}
