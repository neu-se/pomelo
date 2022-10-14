package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.meringue.*;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.AbstractSurefireMojo;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;

import java.io.File;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

class TestFuzzer {
    private final SourcesResolver sourcesResolver;
    private final AnalysisRunner analysisRunner;
    private final CampaignRunner campaignRunner;
    private final CampaignConfiguration configuration;

    TestFuzzer(AbstractSurefireMojo mojo, String testClass, String testMethod, String duration, File outputDirectory,
               int maxTraceSize, boolean debug, long timeout, boolean verbose, File temporaryDirectory,
               ArtifactResolver artifactResolver,
               ArtifactHandlerManager artifactHandlerManager) throws MojoExecutionException {
        this.sourcesResolver =
                new SourcesResolver(mojo.getLog(), mojo.getSession(), artifactResolver, artifactHandlerManager);
        JvmConfiguration configuration = new SurefireMojoWrapper(mojo).extractJvmConfiguration();
        CoverageFilter filter =
                new CoverageFilter(Collections.emptyList(), Collections.singletonList("edu/neu/ccs/prl/pomelo/**"),
                                   Collections.emptyList());
        File campaignDirectory = new File(outputDirectory, "campaign");
        PluginUtil.ensureEmptyDirectory(campaignDirectory);
        PluginUtil.ensureEmptyDirectory(temporaryDirectory);
        JvmConfiguration config = new SurefireMojoWrapper(mojo).extractJvmConfiguration();
        File manifestJar = config.buildManifestJar(temporaryDirectory, Collections.emptyList());
        File propertiesFile = config.writeSystemProperties(temporaryDirectory, 0);
        // TODO split fuzz and analysis tasks
        // TODO set system property -Dpomelo.properties=<path-to-props-file>
        this.campaignRunner = new CampaignRunner(mojo.getLog(), Duration.parse(duration));
        this.analysisRunner =
                new AnalysisRunner(sourcesResolver, mojo.getLog(), debug, verbose, Duration.ofMillis(timeout),
                                   maxTraceSize, filter, outputDirectory, temporaryDirectory, mojo.getProject(),
                                   configuration.getTestClasspathElements());
        this.configuration = new CampaignConfiguration(
                testClass, testMethod, Duration.parse(duration), campaignDirectory,
                configuration.getJavaOptions(0),
                manifestJar, configuration.getJavaExecutable(), configuration.getWorkingDirectory(0),
                configuration.getEnvironment()
        );

    }

    public void execute() throws MojoExecutionException {
        // TODO create meringue FuzzFramework for Pomelo
        campaignRunner.run(configuration, "", new Properties());
        analysisRunner.run(configuration, "", new Properties());
    }
}
