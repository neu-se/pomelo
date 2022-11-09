package edu.neu.ccs.prl.pomelo.fuzz;

import edu.neu.ccs.prl.meringue.AnalysisValues;
import edu.neu.ccs.prl.meringue.CampaignValues;
import edu.neu.ccs.prl.meringue.JacocoReportFormat;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.resolver.ResolutionErrorHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.failsafe.IntegrationTestMojo;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;

import java.io.File;
import java.util.List;

@Mojo(name = "fuzz-integration-test", requiresDependencyResolution = ResolutionScope.TEST,
        defaultPhase = LifecyclePhase.INTEGRATION_TEST)
public class FailsafeFuzzingMojo extends IntegrationTestMojo {
    /**
     * {@link CampaignValues#getTestClassName()}
     */
    @Parameter(property = "pomelo.testClass", required = true)
    private String testClass;
    /**
     * {@link CampaignValues#getTestMethodName()}
     */
    @Parameter(property = "pomelo.testMethod", required = true)
    private String testMethod;
    /**
     * {@link CampaignValues#getDurationString()}
     */
    @Parameter(property = "pomelo.duration", defaultValue = "P1D")
    private String duration;
    /**
     * {@link CampaignValues#getOutputDirectory()}
     */
    @Parameter(property = "pomelo.outputDirectory", defaultValue = "${project.build.directory}/pomelo/fuzz/out")
    private File outputDirectory;
    /**
     * {@link CampaignValues#getTemporaryDirectory()}
     */
    @Parameter(defaultValue = "${project.build.directory}/pomelo/fuzz/temp", readonly = true, required = true)
    private File temporaryDirectory;
    /**
     * {@link CampaignValues#getErrorHandler()}
     */
    @Component
    private ResolutionErrorHandler errorHandler;
    /**
     * {@link AnalysisValues#isVerbose()}
     */
    @Parameter(property = "pomelo.maxTraceSize", defaultValue = "5")
    private int maxTraceSize;
    /**
     * {@link AnalysisValues#isDebug()}
     */
    @Parameter(property = "pomelo.debug", defaultValue = "false")
    private boolean debug;
    /**
     * {@link AnalysisValues#getTimeout()}
     */
    @Parameter(property = "pomelo.timeout", defaultValue = "600")
    private long timeout;
    /**
     * {@link AnalysisValues#isVerbose()}
     */
    @Parameter(property = "pomelo.verbose", defaultValue = "false")
    private boolean verbose;
    /**
     * {@link AnalysisValues#getJacocoFormats()}
     */
    @Parameter(property = "pomelo.jacocoFormats", defaultValue = "HTML,CSV,XML")
    private List<JacocoReportFormat> jacocoFormats;
    /**
     * {@link AnalysisValues#getArtifactResolver()}
     */
    @Component
    private ArtifactResolver artifactResolver;
    /**
     * {@link AnalysisValues#getArtifactHandlerManager()}
     */
    @Component
    private ArtifactHandlerManager artifactHandlerManager;

    @Override
    public void execute() throws MojoExecutionException {
        PomeloCampaignValues campaignValues =
                new PomeloCampaignValues(this, testClass, testMethod, duration, outputDirectory, temporaryDirectory,
                                         errorHandler);
        campaignValues.fuzz();
        PomeloAnalysisValues analysisValues =
                new PomeloAnalysisValues(campaignValues, debug, maxTraceSize, verbose, jacocoFormats,
                                         artifactHandlerManager, artifactResolver, timeout);
        analysisValues.analyze();
    }
}