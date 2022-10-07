package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.meringue.JvmLauncher;
import edu.neu.ccs.prl.meringue.ProcessUtil;
import edu.neu.ccs.prl.pomelo.scan.ScanForkMain;
import edu.neu.ccs.prl.pomelo.scan.TestRecord;
import edu.neu.ccs.prl.pomelo.util.SystemPropertyUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.JdkAttributes;
import org.apache.maven.plugin.surefire.booterclient.ForkConfiguration;
import org.apache.maven.plugin.surefire.booterclient.Platform;
import org.apache.maven.surefire.booter.StartupConfiguration;
import org.apache.maven.surefire.booter.SurefireBooterForkException;
import org.apache.maven.surefire.shared.utils.cli.Commandline;
import org.apache.maven.surefire.shared.utils.cli.ShutdownHookUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TestLauncher {
    private final File workingDir;
    private final String[] environment;
    private final JvmLauncher.JarLauncher launcher;
    private final File propertiesFile;

    TestLauncher(File workingDir, String[] environment, File propertiesFile, JvmLauncher.JarLauncher launcher) {
        if (environment == null || launcher == null) {
            throw new NullPointerException();
        }
        if (!propertiesFile.isFile()) {
            throw new IllegalArgumentException("Properties file must be a file: " + propertiesFile);
        }
        if (workingDir != null && !workingDir.isDirectory()) {
            throw new IllegalArgumentException("Working directory must be a directory: " + workingDir);
        }
        this.workingDir = workingDir;
        this.environment = environment;
        this.propertiesFile = propertiesFile;
        this.launcher = launcher;
    }

    public Process launchScan(TestRecord record, File reportFile) throws MojoExecutionException {
        String[] arguments = {record.getTestClassName(), record.getTestMethodName(),
                reportFile.getAbsolutePath(),
                propertiesFile.getAbsolutePath()};
        JvmLauncher mainLauncher =
                new JvmLauncher.JavaMainLauncher(launcher.getJavaExec(), ScanForkMain.class.getName(),
                                                 launcher.getOptions(), true, arguments)
                        .appendOptions("-cp", launcher.getJar().getAbsolutePath());
        ProcessBuilder builder = new ProcessBuilder(mainLauncher.createCommand()).directory(workingDir);
        setEnvironment(builder, environment);
        try {
            return ProcessUtil.start(builder, true);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to create test fork", e);
        }
    }

    private static void setEnvironment(ProcessBuilder builder, String[] environment) {
        Map<String, String> map = builder.environment();
        map.clear();
        for (String entry : environment) {
            int index = entry.indexOf('=');
            if (index != -1) {
                map.put(entry.substring(0, index), entry.substring(index + 1));
            }
        }
    }

    public static TestLauncher create(SurefireMojoWrapper mojo, File outputDir) throws MojoExecutionException {
        JdkAttributes jdkAttributes = mojo.getJdkAttributes();
        Commandline commandline = createCommandline(mojo, jdkAttributes, outputDir);
        File propertiesFile = writeProperties(mojo, outputDir);
        return new TestLauncher(commandline.getWorkingDirectory(), commandline.getEnvironmentVariables(),
                                propertiesFile, extractJarLauncher(commandline, jdkAttributes));
    }

    private static File writeProperties(SurefireMojoWrapper mojo, File outputDir) throws MojoExecutionException {
        Properties javaSystemProperties = mojo.setupProperties();
        File propsFile = PluginUtil.ensureNew(new File(outputDir, "pomelo.properties"));
        try {
            SystemPropertyUtil.store(propsFile, "system", javaSystemProperties);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write properties file: " + propsFile, e);
        }
        return propsFile;
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
