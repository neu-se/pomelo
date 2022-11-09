package edu.neu.ccs.prl.pomelo.fuzz;

import edu.neu.ccs.prl.meringue.CampaignValues;
import edu.neu.ccs.prl.meringue.JvmLauncher;
import edu.neu.ccs.prl.pomelo.JvmConfiguration;
import edu.neu.ccs.prl.pomelo.PluginUtil;
import edu.neu.ccs.prl.pomelo.SurefireMojoWrapper;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ResolutionErrorHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.surefire.AbstractSurefireMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;

import java.io.File;
import java.util.*;

public class PomeloCampaignValues implements CampaignValues {
    private final MavenSession session;
    private final MavenProject project;
    private final String testClassName;
    private final String testMethodName;
    private final String durationString;
    private final File outputDirectory;
    private final File temporaryDirectory;
    private final ResolutionErrorHandler errorHandler;
    private final AbstractSurefireMojo mojo;
    private final JvmConfiguration configuration;

    public PomeloCampaignValues(AbstractSurefireMojo mojo,
                                String testClassName, String testMethodName, String durationString,
                                File outputDirectory, File temporaryDirectory, ResolutionErrorHandler errorHandler)
            throws MojoExecutionException {
        this.session = mojo.getSession();
        this.project = mojo.getProject();
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
        this.durationString = durationString;
        this.outputDirectory = outputDirectory;
        this.temporaryDirectory = temporaryDirectory;
        this.errorHandler = errorHandler;
        this.mojo = mojo;
        this.configuration = new SurefireMojoWrapper(mojo).extractJvmConfiguration();
    }

    @Override
    public MavenSession getSession() {
        return session;
    }

    @Override
    public MavenProject getProject() {
        return project;
    }

    @Override
    public File getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    public String getTestClassName() {
        return testClassName;
    }

    @Override
    public String getTestMethodName() {
        return testMethodName;
    }

    @Override
    public File getJavaExecutable() {
        return configuration.getJavaExecutable();
    }

    @Override
    public String getFrameworkClassName() {
        return PomeloFuzzFramework.class.getName();
    }

    @Override
    public Properties getFrameworkArguments() {
        return new Properties();
    }

    @Override
    public List<String> getJavaOptions() throws MojoExecutionException {
        return getJavaOptions(true);
    }

    @Override
    public String getDurationString() {
        return durationString;
    }

    @Override
    public File getTemporaryDirectory() {
        return temporaryDirectory;
    }

    @Override
    public ArtifactRepository getLocalRepository() {
        return mojo.getLocalRepository();
    }

    @Override
    public Map<String, Artifact> getPluginArtifactMap() {
        return mojo.getPluginArtifactMap();
    }

    @Override
    public ResolutionErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
    public RepositorySystem getRepositorySystem() {
        return mojo.getRepositorySystem();
    }

    @Override
    public Log getLog() {
        return mojo.getLog();
    }

    @Override
    public Map<String, String> getEnvironment() {
        return configuration.getEnvironment();
    }

    @Override
    public File getWorkingDirectory() throws MojoExecutionException {
        return configuration.getWorkingDirectory(1);
    }

    @Override
    public Set<File> getTestClasspathElements() {
        return new HashSet<>(configuration.getTestClasspathElements());
    }

    public List<String> getJavaOptions(boolean includeDebug) throws MojoExecutionException {
        File systemPropertiesFile = new File(temporaryDirectory, "pomelo1.properties");
        PluginUtil.writeProperties(configuration.getSystemProperties(1), systemPropertiesFile);
        List<String> options = configuration.getJavaOptions(1);
        if (includeDebug && configuration.isDebug()) {
            options.add(JvmLauncher.DEBUG_OPT + "5005");
        }
        options.add(String.format("-D%s=%s", FuzzForkMain.SYSTEM_PROPERTIES_KEY,
                                  systemPropertiesFile.getAbsolutePath()));
        return options;
    }
}
