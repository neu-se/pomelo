package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.meringue.FileUtil;
import edu.neu.ccs.prl.meringue.JvmLauncher;
import edu.neu.ccs.prl.pomelo.scan.ScanForkMain;
import edu.neu.ccs.prl.pomelo.util.SystemPropertyUtil;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class TestLauncher {
    private final File jar;
    private final TestJvmConfiguration configuration;
    private final File temporaryDir;

    public TestLauncher(SurefireMojoWrapper mojo, File temporaryDir) throws MojoExecutionException {
        this.configuration = mojo.extractTestJvmConfiguration();
        this.temporaryDir = temporaryDir;

        this.jar = new File(temporaryDir, "test.jar");
        try {
            FileUtil.buildManifestJar(configuration.getTestClassPathElements(), jar);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to build test classpath manifest JAR", e);
        }
    }

    public Process launchScanFork(String testClass, String testMethod, File report) throws MojoExecutionException {
        int forkNumber = 0;
        File propertiesFile = writeProperties(configuration.getSystemProperties(forkNumber),
                                              new File(temporaryDir, "pomelo" + forkNumber + ".properties"));
        List<String> options = configuration.getJavaOptions(forkNumber);
        if (configuration.isDebug()) {
            options.add(JvmLauncher.DEBUG_OPT + "5005");
        }
        options.add("-cp");
        options.add(jar.getAbsolutePath());
        try {
            JvmLauncher launcher = JvmLauncher.fromMain(configuration.getJavaExecutable(), ScanForkMain.class.getName(),
                                                        options.toArray(new String[0]), true, new String[0],
                                                        configuration.getWorkingDirectory(forkNumber),
                                                        configuration.getEnvironment())
                                              .withArguments(testClass, testMethod, propertiesFile.getAbsolutePath(),
                                                             report.getAbsolutePath());
            return launcher.launch();
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to create test fork", e);
        }
    }

    private static File writeProperties(Properties properties, File file) throws MojoExecutionException {
        PluginUtil.ensureNew(file);
        try {
            SystemPropertyUtil.store(file, "system", properties);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write properties to file: " + file, e);
        }
        return file;
    }
}
