package edu.neu.ccs.prl.pomelo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.failsafe.IntegrationTestMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "fuzz-integration-test", requiresDependencyResolution = ResolutionScope.TEST,
        defaultPhase = LifecyclePhase.INTEGRATION_TEST)
public class FailsafeFuzzingMojo extends IntegrationTestMojo {
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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        setTest(testClass + "#" + testMethod);
        getLog().info(String.format("Fuzzing %s", getTest()));
        super.execute();
    }
}