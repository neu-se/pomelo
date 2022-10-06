package edu.neu.ccs.prl.pomelo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.AbstractSurefireMojo;
import org.apache.maven.plugin.surefire.JdkAttributes;
import org.apache.maven.plugin.surefire.SurefireHelper;
import org.apache.maven.plugin.surefire.SurefireProperties;
import org.apache.maven.plugin.surefire.booterclient.ForkConfiguration;
import org.apache.maven.plugin.surefire.booterclient.Platform;
import org.apache.maven.plugin.surefire.log.PluginConsoleLogger;
import org.apache.maven.surefire.api.cli.CommandLineOption;
import org.apache.maven.surefire.api.util.DefaultScanResult;
import org.apache.maven.surefire.booter.ClassLoaderConfiguration;
import org.apache.maven.surefire.booter.StartupConfiguration;
import org.apache.maven.surefire.providerapi.ProviderInfo;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
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

    public void addScanListener(File report, String plugin, String execution) throws MojoExecutionException {
        getProperties().put("listener", PomeloJUnitListener.class.getName());
        Properties systemProperties = getSystemProperties();
        systemProperties.put("pomelo.scan.report", report.getAbsolutePath());
        systemProperties.put("pomelo.scan.project", mojo.getProject().getFile().getAbsolutePath());
        systemProperties.put("pomelo.scan.plugin", plugin);
        systemProperties.put("pomelo.scan.execution", execution);
    }

    public SurefireProperties setupProperties() throws MojoExecutionException {
        return invokeMethod(mojo, getMethod("setupProperties"), SurefireProperties.class);
    }

    public Object findModuleDescriptor(File jdkHome) throws MojoExecutionException {
        return invokeMethod(mojo, getMethod("findModuleDescriptor", File.class), Object.class, jdkHome);
    }

    public StartupConfiguration createStartupConfiguration(DefaultScanResult scanResult, Platform platform,
                                                           Object resolvedJavaModularity)
            throws MojoExecutionException {
        Class<?> testClassPathClass = findClass("org.apache.maven.plugin.surefire.TestClassPath");
        Object testClassPath = invokeMethod(mojo, getMethod("generateTestClasspath"), Object.class);
        ProviderInfo provider = new EmptyProviderInfo();
        ClassLoaderConfiguration classLoaderConfiguration =
                invokeMethod(mojo, getMethod("getClassLoaderConfiguration"), ClassLoaderConfiguration.class);
        Class<?> resolvedPathClass = findClass("org.apache.maven.plugin.surefire.ResolvePathResultWrapper");
        Method method = getMethod("createStartupConfiguration", ProviderInfo.class, boolean.class,
                                  ClassLoaderConfiguration.class, DefaultScanResult.class, testClassPathClass,
                                  Platform.class, resolvedPathClass);
        return invokeMethod(mojo, method, StartupConfiguration.class, provider, true, classLoaderConfiguration,
                            scanResult, testClassPath, platform, resolvedJavaModularity);
    }

    public ForkConfiguration createForkConfiguration(Platform platform, Object resolvedJavaModularity)
            throws MojoExecutionException {
        Class<?> resolvedPathClass = findClass("org.apache.maven.plugin.surefire.ResolvePathResultWrapper");
        Method method = getMethod("createForkConfiguration", Platform.class, resolvedPathClass);
        return invokeMethod(mojo, method, ForkConfiguration.class, platform, resolvedJavaModularity);
    }

    public void forceForks() throws MojoExecutionException {
        mojo.setForkMode("once");
        setField(mojo, "forkCount", "1");
        setField(mojo, "reuseForks", true);
    }

    public JdkAttributes getJdkAttributes() throws MojoExecutionException {
        return invokeMethod(mojo, getMethod("getEffectiveJvm"), JdkAttributes.class);
    }

    public PluginConsoleLogger getConsoleLogger() throws MojoExecutionException {
        return invokeMethod(mojo, getMethod("getConsoleLogger"), PluginConsoleLogger.class);
    }

    public void configure() throws MojoExecutionException {
        List<CommandLineOption> options = SurefireHelper.commandLineOptions(mojo.getSession(), getConsoleLogger());
        setField(mojo, "cli", options);
        invokeMethod(mojo, getMethod("setupStuff"), void.class);
    }

    public boolean verifyParameters() throws MojoExecutionException {
        return invokeMethod(mojo, getMethod("verifyParameters"), Boolean.class);
    }

    public DefaultScanResult scanForTestClasses() throws MojoExecutionException {
        return invokeMethod(mojo, getMethod("scanForTestClasses"), DefaultScanResult.class);
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
}
