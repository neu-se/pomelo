package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.pomelo.util.FileUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.surefire.AbstractSurefireMojo;
import org.apache.maven.plugin.surefire.JdkAttributes;
import org.apache.maven.plugin.surefire.SurefireHelper;
import org.apache.maven.plugin.surefire.SurefireProperties;
import org.apache.maven.plugin.surefire.booterclient.JarManifestForkConfiguration;
import org.apache.maven.plugin.surefire.booterclient.Platform;
import org.apache.maven.plugin.surefire.extensions.LegacyForkNodeFactory;
import org.apache.maven.plugin.surefire.log.PluginConsoleLogger;
import org.apache.maven.project.MavenProject;
import org.apache.maven.surefire.api.cli.CommandLineOption;
import org.apache.maven.surefire.booter.ClassLoaderConfiguration;
import org.apache.maven.surefire.booter.Classpath;
import org.apache.maven.surefire.booter.StartupConfiguration;
import org.apache.maven.surefire.providerapi.ProviderInfo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

public final class SurefireMojoWrapper {
    private final AbstractSurefireMojo mojo;
    private final PluginExecutable execute;

    public SurefireMojoWrapper(AbstractSurefireMojo mojo, PluginExecutable execute) {
        if (mojo == null || execute == null) {
            throw new NullPointerException();
        }
        this.mojo = mojo;
        this.execute = execute;
    }

    public Properties getProperties() throws MojoExecutionException {
        Properties properties = invokeMethod(mojo, getMethod("getProperties"), Properties.class);
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

    public SurefireProperties setupProperties() throws MojoExecutionException {
        return invokeMethod(mojo, getMethod("setupProperties"), SurefireProperties.class);
    }

    public StartupConfiguration createStartupConfiguration() throws MojoExecutionException {
        Class<?> testClassPathClass = findClass("org.apache.maven.plugin.surefire.TestClassPath");
        Object testClassPath = invokeMethod(mojo, getMethod("generateTestClasspath"), Object.class);
        ClassLoaderConfiguration classLoaderConfiguration =
                invokeMethod(mojo, getMethod("getClassLoaderConfiguration"), ClassLoaderConfiguration.class);
        Method method = getMethod("newStartupConfigWithClasspath", ClassLoaderConfiguration.class, ProviderInfo.class,
                                  testClassPathClass);
        return invokeMethod(mojo, method, StartupConfiguration.class, classLoaderConfiguration, new EmptyProviderInfo(),
                            testClassPath);
    }

    public JarManifestForkConfiguration createForkConfiguration(Platform platform, File tempDir)
            throws MojoExecutionException {
        Classpath bootClasspath = Classpath.emptyClasspath();
        File workingDir = mojo.getWorkingDirectory() != null ? mojo.getWorkingDirectory() : mojo.getBasedir();
        return new JarManifestForkConfiguration(bootClasspath, tempDir, null, workingDir,
                                                getProject().getModel().getProperties(), mojo.getArgLine(),
                                                mojo.getEnvironmentVariables(), getExcludedEnvironmentVariables(),
                                                false, 1, true, platform, getConsoleLogger(),
                                                new LegacyForkNodeFactory());
    }

    public String[] getExcludedEnvironmentVariables() throws MojoExecutionException {
        return invokeMethod(mojo, getMethod("getExcludedEnvironmentVariables"), String[].class);
    }

    public JdkAttributes getJdkAttributes() throws MojoExecutionException {
        return invokeMethod(mojo, getMethod("getEffectiveJvm"), JdkAttributes.class);
    }

    public PluginConsoleLogger getConsoleLogger() throws MojoExecutionException {
        return invokeMethod(mojo, getMethod("getConsoleLogger"), PluginConsoleLogger.class);
    }

    public void configure() throws MojoExecutionException {
        // Force fork
        mojo.setForkMode("once");
        setField(mojo, "forkCount", "1");
        setField(mojo, "reuseForks", true);
        // Disable skips
        mojo.setSkipTests(false);
        mojo.setSkip(false);
        mojo.setSkipExec(false);
        // Initialize configuration values
        List<CommandLineOption> options = SurefireHelper.commandLineOptions(mojo.getSession(), getConsoleLogger());
        setField(mojo, "cli", options);
        invokeMethod(mojo, getMethod("setupStuff"), void.class);
        invokeMethod(mojo, getMethod("verifyParameters"), Boolean.class);
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        execute.execute();
    }

    public MavenProject getProject() {
        return mojo.getProject();
    }

    public static File ensureNew(File file) throws MojoExecutionException {
        try {
            return FileUtil.ensureNew(file);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to create file: " + file, e);
        }
    }

    private static void setField(AbstractSurefireMojo mojo, String fieldName, Object value)
            throws MojoExecutionException {
        try {
            Field field = AbstractSurefireMojo.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            if (field.getType().equals(boolean.class)) {
                field.setBoolean(mojo, (Boolean) value);
            } else {
                field.set(mojo, value);
            }
        } catch (ReflectiveOperationException e) {
            throw new MojoExecutionException("Failed to set field " + fieldName, e);
        }
    }

    private static Class<?> findClass(String className) throws MojoExecutionException {
        try {
            return Class.forName(className, true, AbstractSurefireMojo.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Unable to find class: " + className, e);
        }
    }

    private static Method getMethod(String methodName, Class<?>... parameterTypes) throws MojoExecutionException {
        try {
            Method method = AbstractSurefireMojo.class.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (ClassCastException | ReflectiveOperationException e) {
            throw new MojoExecutionException("Failed to find method " + methodName, e);
        }
    }

    private static <T> T invokeMethod(AbstractSurefireMojo mojo, Method method, Class<T> returnType, Object... args)
            throws MojoExecutionException {
        try {
            return returnType.cast(method.invoke(mojo, args));
        } catch (ClassCastException | ReflectiveOperationException e) {
            throw new MojoExecutionException("Failed to invoke method " + method, e);
        }
    }

    public interface PluginExecutable {
        void execute() throws MojoExecutionException, MojoFailureException;
    }
}
