package edu.neu.ccs.prl.pomelo.fuzz;

import edu.neu.ccs.prl.meringue.AnalysisValues;
import edu.neu.ccs.prl.meringue.JacocoReportFormat;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ResolutionErrorHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;

import java.io.File;
import java.util.*;

public class PomeloAnalysisValues implements AnalysisValues {
    private final PomeloCampaignValues campaignValues;
    private final boolean debug;
    private final int maxTraceSize;
    private final boolean verbose;
    private final List<JacocoReportFormat> jacocoFormats;
    private final ArtifactHandlerManager artifactHandlerManager;
    private final ArtifactResolver artifactResolver;
    private final long timeout;

    public PomeloAnalysisValues(PomeloCampaignValues campaignValues, boolean debug, int maxTraceSize, boolean verbose,
                                List<JacocoReportFormat> jacocoFormats, ArtifactHandlerManager artifactHandlerManager,
                                ArtifactResolver artifactResolver, long timeout) {
        this.campaignValues = campaignValues;
        this.debug = debug;
        this.maxTraceSize = maxTraceSize;
        this.verbose = verbose;
        this.jacocoFormats = jacocoFormats;
        this.artifactHandlerManager = artifactHandlerManager;
        this.artifactResolver = artifactResolver;
        this.timeout = timeout;
    }

    @Override
    public MavenSession getSession() {
        return campaignValues.getSession();
    }

    @Override
    public MavenProject getProject() {
        return campaignValues.getProject();
    }

    @Override
    public File getOutputDirectory() {
        return campaignValues.getOutputDirectory();
    }

    @Override
    public String getTestClassName() {
        return campaignValues.getTestClassName();
    }

    @Override
    public String getTestMethodName() {
        return campaignValues.getTestMethodName();
    }

    @Override
    public File getJavaExecutable() {
        return campaignValues.getJavaExecutable();
    }

    @Override
    public String getFrameworkClassName() {
        return campaignValues.getFrameworkClassName();
    }

    @Override
    public Properties getFrameworkArguments() {
        return campaignValues.getFrameworkArguments();
    }

    @Override
    public List<String> getJavaOptions() throws MojoExecutionException {
        return campaignValues.getJavaOptions(false);
    }

    @Override
    public String getDurationString() {
        return campaignValues.getDurationString();
    }

    @Override
    public File getTemporaryDirectory() {
        return campaignValues.getTemporaryDirectory();
    }

    @Override
    public ArtifactRepository getLocalRepository() {
        return campaignValues.getLocalRepository();
    }

    @Override
    public Map<String, Artifact> getPluginArtifactMap() {
        return campaignValues.getPluginArtifactMap();
    }

    @Override
    public ResolutionErrorHandler getErrorHandler() {
        return campaignValues.getErrorHandler();
    }

    @Override
    public RepositorySystem getRepositorySystem() {
        return campaignValues.getRepositorySystem();
    }

    @Override
    public Log getLog() {
        return campaignValues.getLog();
    }

    @Override
    public Map<String, String> getEnvironment() {
        return campaignValues.getEnvironment();
    }

    @Override
    public File getWorkingDirectory() throws MojoExecutionException {
        return campaignValues.getWorkingDirectory();
    }

    @Override
    public Set<File> getTestClasspathElements() {
        return campaignValues.getTestClasspathElements();
    }

    @Override
    public List<String> getIncludedArtifacts() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getInclusions() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getExclusions() {
        return Collections.singletonList("edu/neu/ccs/prl/pomelo/**");
    }

    @Override
    public int getMaxTraceSize() {
        return maxTraceSize;
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    @Override
    public boolean isVerbose() {
        return verbose;
    }

    @Override
    public List<JacocoReportFormat> getJacocoFormats() {
        return jacocoFormats;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public ArtifactResolver getArtifactResolver() {
        return artifactResolver;
    }

    @Override
    public ArtifactHandlerManager getArtifactHandlerManager() {
        return artifactHandlerManager;
    }
}
