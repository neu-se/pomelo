package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.meringue.CampaignConfiguration;
import edu.neu.ccs.prl.meringue.CampaignRunner;
import edu.neu.ccs.prl.meringue.JvmLauncher;
import edu.neu.ccs.prl.pomelo.fuzz.PomeloFuzzFramework;
import org.apache.maven.artifact.resolver.ResolutionErrorHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.AbstractSurefireMojo;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class PomeloFuzzer {
    private final AbstractSurefireMojo mojo;
    private final String testClass;
    private final String testMethod;
    private final File outputDirectory;
    private final File temporaryDirectory;
    private final JvmConfiguration configuration;
    private final DependencyResolver dependencyResolver;
    private final Duration duration;

    PomeloFuzzer(AbstractSurefireMojo mojo, String testClass, String testMethod, String duration, File outputDirectory,
                 File temporaryDirectory, ResolutionErrorHandler errorHandler) throws MojoExecutionException {
        this.mojo = mojo;
        this.testClass = testClass;
        this.testMethod = testMethod;
        this.duration = Duration.parse(duration);
        this.outputDirectory = outputDirectory;
        this.temporaryDirectory = temporaryDirectory;
        this.dependencyResolver = new DependencyResolver(mojo.getRepositorySystem(), mojo.getLocalRepository(),
                                                         mojo.getRemoteRepositories(), errorHandler,
                                                         mojo.getSession().isOffline());
        this.configuration = new SurefireMojoWrapper(mojo).extractJvmConfiguration();
    }

    public void fuzz() throws MojoExecutionException {
        PluginUtil.ensureEmptyDirectory(temporaryDirectory);
        File campaignDirectory = new File(outputDirectory, "campaign");
        PluginUtil.ensureEmptyDirectory(campaignDirectory);
        Properties frameworkProperties = createFrameworkProperties();
        new CampaignRunner(mojo.getLog(), duration).run(createCampaignConfiguration(campaignDirectory, true),
                                                        PomeloFuzzFramework.class.getName(), frameworkProperties);
    }


    Properties createFrameworkProperties() throws MojoExecutionException {
        File jar = new File(temporaryDirectory, "framework.jar");
        PluginUtil.buildManifestJar(dependencyResolver.resolve(
                mojo.getPluginArtifactMap().get("edu.neu.ccs.prl.pomelo:pomelo-meringue-extension")), jar);
        Properties properties = new Properties();
        properties.put("frameworkJar", jar.getAbsolutePath());
        return properties;
    }

    CampaignConfiguration createCampaignConfiguration(File campaignDirectory, boolean includeDebug)
            throws MojoExecutionException {
        File testClasspathJar = new File(temporaryDirectory, "test.jar");
        PluginUtil.buildManifestJar(configuration.getTestClasspathElements(), testClasspathJar);
        File file = new File(temporaryDirectory, "pomelo1.properties");
        PluginUtil.writeProperties(configuration.getSystemProperties(1), file);
        List<String> options = configuration.getJavaOptions(1);
        if (includeDebug && configuration.isDebug()) {
            options.add(JvmLauncher.DEBUG_OPT + "5005");
        }
        options.add("-Dpomelo.properties=" + file.getAbsolutePath());
        return new CampaignConfiguration(testClass, testMethod, duration, campaignDirectory, options, testClasspathJar,
                                         configuration.getJavaExecutable(), configuration.getWorkingDirectory(1),
                                         configuration.getEnvironment());
    }

    public JvmConfiguration getConfiguration() {
        return configuration;
    }
}
