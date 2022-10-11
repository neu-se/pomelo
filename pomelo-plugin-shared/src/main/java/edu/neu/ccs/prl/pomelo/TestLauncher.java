package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.meringue.JvmLauncher;
import edu.neu.ccs.prl.pomelo.scan.ScanForkMain;
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
    private final File propertiesFile;
    private final Map<String, String> environment;
    private final File workingDirectory;
    private final File javaExec;
    private final File jar;
    private final String[] options;

    public TestLauncher(SurefireMojoWrapper mojo, File tempDir) throws MojoExecutionException {
        boolean enableAssertions = mojo.effectiveIsEnableAssertions();
        JdkAttributes jdkAttributes = mojo.getJdkAttributes();
        Commandline commandline = createCommandline(mojo, jdkAttributes, tempDir);
        // Find the JAR specified in the options
        List<String> optionList = new LinkedList<>(Arrays.asList(commandline.getArguments()));
        int position = optionList.indexOf("-jar");
        if (position == -1 || position + 1 >= optionList.size()) {
            throw new MojoExecutionException("Unable to location manifest JAR");
        }
        this.jar = new File(optionList.remove(position + 1));
        optionList.remove(position);
        if (enableAssertions) {
            optionList.add("-ea");
        }
        this.propertiesFile = writeProperties(mojo, tempDir);
        this.options = optionList.toArray(new String[0]);
        this.environment = convertEnvironment(commandline.getEnvironmentVariables());
        this.workingDirectory = commandline.getWorkingDirectory();
        this.javaExec = jdkAttributes.getJvmExecutable();
    }

    public Process launchScanFork(String testClass, String testMethod, File report) throws MojoExecutionException {
        String[] arguments =
                new String[]{testClass, testMethod, propertiesFile.getAbsolutePath(), report.getAbsolutePath()};
        try {
            return JvmLauncher.fromMain(javaExec, ScanForkMain.class.getName(), options, true, arguments,
                                        workingDirectory, environment)
                              .appendOptions("-cp", jar.getAbsolutePath())
                              .launch();
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to create test fork", e);
        }
    }

    private static Map<String, String> convertEnvironment(String[] environment) {
        Map<String, String> map = new HashMap<>();
        for (String entry : environment) {
            int index = entry.indexOf('=');
            if (index != -1) {
                map.put(entry.substring(0, index), entry.substring(index + 1));
            }
        }
        return map;
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
