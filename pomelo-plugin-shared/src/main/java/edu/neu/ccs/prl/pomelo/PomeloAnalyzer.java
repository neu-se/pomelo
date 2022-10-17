package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.meringue.AnalysisRunner;
import edu.neu.ccs.prl.meringue.CoverageFilter;
import edu.neu.ccs.prl.meringue.SourcesResolver;
import edu.neu.ccs.prl.pomelo.fuzz.PomeloFuzzFramework;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.resolver.ResolutionErrorHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.AbstractSurefireMojo;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;

import java.io.File;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class PomeloAnalyzer {
    private final AbstractSurefireMojo mojo;
    private final File outputDirectory;
    private final File temporaryDirectory;
    private final Duration timeout;
    private final SourcesResolver sourcesResolver;
    private final PomeloFuzzer fuzzer;
    private final int maxTraceSize;
    private final boolean verbose;
    private final boolean debug;

    PomeloAnalyzer(AbstractSurefireMojo mojo, String testClass, String testMethod, String duration,
                   File outputDirectory, int maxTraceSize, boolean debug, long timeout, boolean verbose,
                   File temporaryDirectory, ArtifactResolver artifactResolver,
                   ArtifactHandlerManager artifactHandlerManager, ResolutionErrorHandler errorHandler)
            throws MojoExecutionException {
        this.debug = debug;
        this.fuzzer =
                new PomeloFuzzer(mojo, testClass, testMethod, duration, outputDirectory, temporaryDirectory,
                                 errorHandler, quiet);
        this.maxTraceSize = maxTraceSize;
        this.verbose = verbose;
        this.mojo = mojo;
        this.outputDirectory = outputDirectory;
        this.temporaryDirectory = temporaryDirectory;
        this.timeout = Duration.ofMillis(timeout);
        this.sourcesResolver =
                new SourcesResolver(mojo.getLog(), mojo.getSession(), artifactResolver, artifactHandlerManager);
    }

    public void analyze() throws MojoExecutionException {
        File campaignDirectory = new File(outputDirectory, "campaign");
        if (!campaignDirectory.exists()) {
            throw new MojoExecutionException("Campaign directory not found: " + campaignDirectory);
        }
        File meringueDirectory = new File(temporaryDirectory, "meringue");
        PluginUtil.ensureEmptyDirectory(meringueDirectory);
        Properties frameworkProperties = fuzzer.createFrameworkProperties();
        CoverageFilter filter =
                new CoverageFilter(Collections.emptyList(), Collections.singletonList("edu/neu/ccs/prl/pomelo/**"),
                                   Collections.emptyList());
        new AnalysisRunner(sourcesResolver, mojo.getLog(), debug, verbose, timeout, maxTraceSize,
                           filter, outputDirectory, meringueDirectory, mojo.getProject(),
                           fuzzer.getConfiguration().getTestClasspathElements()).run(
                fuzzer.createCampaignConfiguration(campaignDirectory, false), PomeloFuzzFramework.class.getName(),
                frameworkProperties);
    }
}
