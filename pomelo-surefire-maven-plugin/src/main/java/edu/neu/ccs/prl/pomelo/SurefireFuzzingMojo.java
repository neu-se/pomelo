package edu.neu.ccs.prl.pomelo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.JdkAttributes;
import org.apache.maven.plugin.surefire.SurefirePlugin;
import org.apache.maven.plugin.surefire.SurefireProperties;
import org.apache.maven.plugin.surefire.booterclient.ForkConfiguration;
import org.apache.maven.plugin.surefire.booterclient.Platform;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.surefire.api.util.DefaultScanResult;
import org.apache.maven.surefire.booter.StartupConfiguration;
import org.apache.maven.surefire.booter.SurefireBooterForkException;
import org.apache.maven.surefire.shared.utils.cli.Commandline;
import org.apache.maven.surefire.shared.utils.cli.ShutdownHookUtils;

@Mojo(name = "fuzz-test", defaultPhase = LifecyclePhase.TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class SurefireFuzzingMojo extends SurefirePlugin {
    private static final Platform PLATFORM = new Platform();
    private final SurefireMojoWrapper wrapper = new SurefireMojoWrapper(this, super::execute);
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
    public void execute() throws MojoExecutionException {
        setTest(testClass + "#" + testMethod);
        getLog().info(String.format("Fuzzing %s", getTest()));
        wrapper.forceForks();
        wrapper.configure();
        JdkAttributes attributes = wrapper.getJdkAttributes();
        Platform platform = PLATFORM.withJdkExecAttributesForTests(attributes);
        Thread shutdownThread = new Thread(platform::setShutdownState);
        ShutdownHookUtils.addShutDownHook(shutdownThread);
        try {
            if (wrapper.verifyParameters() && !hasExecutedBefore()) {
                DefaultScanResult scan = wrapper.scanForTestClasses();
                if (!scan.isEmpty()) {
                    executeAfterPreconditionsChecked(scan, platform, attributes);
                }
            }
        } finally {
            platform.clearShutdownState();
            ShutdownHookUtils.removeShutdownHook(shutdownThread);
        }
    }

    private void executeAfterPreconditionsChecked(DefaultScanResult scanResult, Platform platform,
                                                  JdkAttributes attributes) throws MojoExecutionException {
        Object moduleDescriptor = wrapper.findModuleDescriptor(attributes.getJdkHome());
        SurefireProperties effectiveProperties = wrapper.setupProperties();
        System.out.print(effectiveProperties);
        StartupConfiguration startupConfiguration =
                wrapper.createStartupConfiguration(scanResult, platform, moduleDescriptor);
        ForkConfiguration forkConfiguration = wrapper.createForkConfiguration(platform, moduleDescriptor);
        try {
            Commandline cli = forkConfiguration.createCommandLine(startupConfiguration, 0, getReportsDirectory());
            System.out.println(cli);
        } catch (SurefireBooterForkException e) {
            throw new MojoExecutionException("Failed to create commandline for test", e);
        } finally {
            cleanupForkConfiguration(forkConfiguration);
        }
    }
}
