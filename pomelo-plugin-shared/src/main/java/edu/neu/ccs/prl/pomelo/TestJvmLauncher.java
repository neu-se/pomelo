package edu.neu.ccs.prl.pomelo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.SurefireProperties;
import org.apache.maven.plugin.surefire.booterclient.ForkConfiguration;
import org.apache.maven.plugin.surefire.booterclient.Platform;
import org.apache.maven.surefire.booter.StartupConfiguration;
import org.apache.maven.surefire.booter.SurefireBooterForkException;
import org.apache.maven.surefire.shared.utils.cli.Commandline;
import org.apache.maven.surefire.shared.utils.cli.ShutdownHookUtils;

import java.io.File;

public class TestJvmLauncher {
    private final String testClassName;
    private final String testMethodName;
    private final File outputDir;
    private final SurefireMojoWrapper wrapper;

    public TestJvmLauncher(String testClassName, String testMethodName, File outputDir, SurefireMojoWrapper wrapper) {
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
        this.outputDir = outputDir;
        this.wrapper = wrapper;
    }

    public void execute() throws MojoExecutionException {
        File tempDir = PluginUtil.createEmptyDirectory(new File(outputDir, "temp"));
        try {
            Commandline commandline = createCommandline(tempDir);
            // check that workingDir is either null or a directory
            File workingDir = commandline.getWorkingDirectory();
            String[] environment = commandline.getEnvironmentVariables();
            String javaExecutable = commandline.getExecutable();
            // javaOptions will include -jar + manifestJar
            String[] javaOptions = commandline.getArguments();
            SurefireProperties javaProperties = wrapper.setupProperties();
        } finally {
            PluginUtil.deleteDirectory(tempDir);
        }
    }

    public Commandline createCommandline(File tempDir) throws MojoExecutionException {
        wrapper.configure();
        Platform platform = new Platform().withJdkExecAttributesForTests(wrapper.getJdkAttributes());
        Thread shutdownThread = new Thread(platform::setShutdownState);
        ShutdownHookUtils.addShutDownHook(shutdownThread);
        try {
            StartupConfiguration startupConfiguration = wrapper.createStartupConfiguration();
            ForkConfiguration forkConfiguration = wrapper.createForkConfiguration(platform, tempDir);
            return forkConfiguration.createCommandLine(startupConfiguration, 0, tempDir);
        } catch (SurefireBooterForkException e) {
            throw new MojoExecutionException("Failed to create commandline for test", e);
        } finally {
            platform.clearShutdownState();
            ShutdownHookUtils.removeShutdownHook(shutdownThread);
        }
    }
}
