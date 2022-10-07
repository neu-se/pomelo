package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.meringue.JvmLauncher;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.JdkAttributes;
import org.apache.maven.plugin.surefire.booterclient.ForkConfiguration;
import org.apache.maven.plugin.surefire.booterclient.Platform;
import org.apache.maven.surefire.booter.StartupConfiguration;
import org.apache.maven.surefire.booter.SurefireBooterForkException;
import org.apache.maven.surefire.shared.utils.cli.Commandline;
import org.apache.maven.surefire.shared.utils.cli.ShutdownHookUtils;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class TestLauncher {
    private final File workingDir;
    private final String[] environment;
    private final Properties javaSystemProperties;
    private final JvmLauncher.JarLauncher launcher;

    public TestLauncher(File workingDir, String[] environment, Properties javaSystemProperties,
                        JvmLauncher.JarLauncher launcher) {
        if (environment == null || javaSystemProperties == null || launcher == null) {
            throw new NullPointerException();
        }
        if (workingDir != null && !workingDir.isDirectory()) {
            throw new IllegalArgumentException("Working directory must be a directory: " + workingDir);
        }
        this.workingDir = workingDir;
        this.environment = environment;
        this.javaSystemProperties = javaSystemProperties;
        this.launcher = launcher;
    }

    public static TestLauncher create(SurefireMojoWrapper mojo, File outputDir) throws MojoExecutionException {
        JdkAttributes jdkAttributes = mojo.getJdkAttributes();
        Commandline commandline = createCommandline(mojo, jdkAttributes, outputDir);
        return new TestLauncher(commandline.getWorkingDirectory(), commandline.getEnvironmentVariables(),
                                mojo.getSystemProperties(), extractJarLauncher(commandline, jdkAttributes));
    }

    private static JvmLauncher.JarLauncher extractJarLauncher(Commandline commandline, JdkAttributes jdkAttributes)
            throws MojoExecutionException {
        // Find the JAR specified in the options
        List<String> options = new LinkedList<>(Arrays.asList(commandline.getArguments()));
        int position = options.indexOf("-jar");
        if (position == -1 || position + 1 >= options.size()) {
            throw new MojoExecutionException("Unable to location manifest JAR");
        }
        File jar = new File(options.remove(position + 1));
        options.remove(position);
        File javaExec = jdkAttributes.getJvmExecutable();
        return new JvmLauncher.JarLauncher(javaExec, jar, options.toArray(new String[0]), false, new String[0]);
    }

    private static Commandline createCommandline(SurefireMojoWrapper mojo, JdkAttributes jdkAttributes, File outputDir)
            throws MojoExecutionException {
        mojo.configure();
        Platform platform = new Platform().withJdkExecAttributesForTests(jdkAttributes);
        Thread shutdownThread = new Thread(platform::setShutdownState);
        ShutdownHookUtils.addShutDownHook(shutdownThread);
        try {
            StartupConfiguration startupConfiguration = mojo.createStartupConfiguration();
            ForkConfiguration forkConfiguration = mojo.createForkConfiguration(platform, outputDir);
            return forkConfiguration.createCommandLine(startupConfiguration, 0, outputDir);
        } catch (SurefireBooterForkException e) {
            throw new MojoExecutionException("Failed to create commandline for test", e);
        } finally {
            platform.clearShutdownState();
            ShutdownHookUtils.removeShutdownHook(shutdownThread);
        }
    }
}
