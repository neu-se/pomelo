package edu.neu.ccs.prl.pomelo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.AbstractSurefireMojo;
import org.apache.maven.plugin.surefire.JdkAttributes;
import org.apache.maven.plugin.surefire.SurefireProperties;
import org.apache.maven.project.MavenProject;
import org.apache.maven.surefire.booter.Classpath;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Properties;

public final class SurefireMojoWrapper {
    private final AbstractSurefireMojo mojo;

    public SurefireMojoWrapper(AbstractSurefireMojo mojo) {
        if (mojo == null) {
            throw new NullPointerException();
        }
        this.mojo = mojo;
    }

    public Properties getProperties() throws MojoExecutionException {
        Properties properties =
                PluginUtil.invokeMethod(PluginUtil.getDeclaredMethod(AbstractSurefireMojo.class, "getProperties"), mojo,
                                        Properties.class);
        if (properties == null) {
            properties = new Properties();
            mojo.setProperties(properties);
        }
        return properties;
    }

    public Properties getSystemProperties() {
        Properties systemProperties = mojo.getSystemProperties();
        if (systemProperties == null) {
            systemProperties = new Properties();
            mojo.setSystemProperties(systemProperties);
        }
        return systemProperties;
    }

    private Classpath getTestClassPath() throws MojoExecutionException {
        Class<?> testClassPathClass = PluginUtil.findClass("org.apache.maven.plugin.surefire.TestClassPath");
        Object testClassPath = PluginUtil.invokeMethod(
                PluginUtil.getDeclaredMethod(AbstractSurefireMojo.class, "generateTestClasspath"), mojo, Object.class);
        Method m = PluginUtil.getDeclaredMethod(testClassPathClass, "toClasspath");
        return PluginUtil.invokeMethod(m, testClassPath, Classpath.class);
    }

    private String[] getExcludedEnvironmentVariables() throws MojoExecutionException {
        return PluginUtil.invokeMethod(
                PluginUtil.getDeclaredMethod(AbstractSurefireMojo.class, "getExcludedEnvironmentVariables"), mojo,
                String[].class);
    }

    private JdkAttributes getJdkAttributes() throws MojoExecutionException {
        return PluginUtil.invokeMethod(PluginUtil.getDeclaredMethod(AbstractSurefireMojo.class, "getEffectiveJvm"),
                                       mojo, JdkAttributes.class);
    }

    public void configure() throws MojoExecutionException {
        // Disable skips
        mojo.setSkipTests(false);
        mojo.setSkip(false);
        mojo.setSkipExec(false);
        // Initialize configuration values
        PluginUtil.invokeMethod(PluginUtil.getDeclaredMethod(AbstractSurefireMojo.class, "setupStuff"), mojo,
                                void.class);
        PluginUtil.invokeMethod(PluginUtil.getDeclaredMethod(AbstractSurefireMojo.class, "verifyParameters"), mojo,
                                Boolean.class);
    }

    public MavenProject getProject() {
        return mojo.getProject();
    }

    public SurefireProperties setupProperties() throws MojoExecutionException {
        return PluginUtil.invokeMethod(PluginUtil.getDeclaredMethod(AbstractSurefireMojo.class, "setupProperties"),
                                       mojo, SurefireProperties.class);
    }

    public TestJvmConfiguration extractTestJvmConfiguration() throws MojoExecutionException {
        configure();
        File workingDir = mojo.getWorkingDirectory() != null ? mojo.getWorkingDirectory() : mojo.getBasedir();
        return new TestJvmConfiguration("true".equals(mojo.getDebugForkedProcess()), mojo.effectiveIsEnableAssertions(),
                                        workingDir, getProject().getModel().getProperties(), mojo.getArgLine(),
                                        mojo.getEnvironmentVariables(), getExcludedEnvironmentVariables(),
                                        getJdkAttributes(), getTestClassPath(), setupProperties());
    }
}
