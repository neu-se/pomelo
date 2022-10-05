package edu.neu.ccs.prl.pomelo;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.*;
import org.apache.maven.plugin.surefire.booterclient.ForkConfiguration;
import org.apache.maven.plugin.surefire.booterclient.Platform;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.surefire.api.cli.CommandLineOption;
import org.apache.maven.surefire.api.util.DefaultScanResult;
import org.apache.maven.surefire.booter.ClassLoaderConfiguration;
import org.apache.maven.surefire.booter.StartupConfiguration;
import org.apache.maven.surefire.booter.SurefireBooterForkException;
import org.apache.maven.surefire.providerapi.ProviderInfo;
import org.apache.maven.surefire.providerapi.ProviderRequirements;
import org.apache.maven.surefire.shared.utils.cli.Commandline;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.apache.maven.surefire.shared.utils.cli.ShutdownHookUtils.addShutDownHook;
import static org.apache.maven.surefire.shared.utils.cli.ShutdownHookUtils.removeShutdownHook;

@Mojo(name = "test", defaultPhase = LifecyclePhase.TEST, threadSafe = true,
        requiresDependencyResolution = ResolutionScope.TEST)
public class SurefireFuzzingMojo extends SurefirePlugin {
    private static final Platform PLATFORM = new Platform();
    /**
     * Fully-qualified name of the test class.
     *
     * @see Class#forName(String className)
     */
    @Parameter(property = "pomelo.testClass", required = true)
    private String testClass;
    /**
     * Name of the test method.
     */
    @Parameter(property = "pomelo.testMethod", required = true)
    private String testMethod;
    /**
     * The current execution of this plugin.
     */
    @Parameter(defaultValue = "${mojoExecution}", readonly = true, required = true)
    private MojoExecution mojoExecution;

    @Override
    public void execute() throws MojoExecutionException {
        forceForks();
        setTest(testClass + "#" + testMethod);
        configure();
        JdkAttributes attributes = invokeMethod(getMethod("getEffectiveJvm"), JdkAttributes.class);
        Platform platform = PLATFORM.withJdkExecAttributesForTests(attributes);
        Thread shutdownThread = new Thread(platform::setShutdownState);
        addShutDownHook(shutdownThread);
        try {
            if (invokeMethod(getMethod("verifyParameters"), Boolean.class) && !hasExecutedBefore()) {
                DefaultScanResult scan = invokeMethod(getMethod("scanForTestClasses"), DefaultScanResult.class);
                if (!scan.isEmpty()) {
                    executeAfterPreconditionsChecked(scan, platform, attributes);
                }
            }
        } finally {
            platform.clearShutdownState();
            removeShutdownHook(shutdownThread);
        }
    }

    private void executeAfterPreconditionsChecked(DefaultScanResult scanResult, Platform platform,
                                                  JdkAttributes attributes) throws MojoExecutionException {
        Object wrapper =
                invokeMethod(getMethod("findModuleDescriptor", File.class), Object.class, attributes.getJdkHome());
        SurefireProperties effectiveProperties = invokeMethod(getMethod("setupProperties"), SurefireProperties.class);
        StartupConfiguration startupConfiguration = createStartupConfiguration(scanResult, platform, wrapper);
        ForkConfiguration forkConfiguration = createForkConfiguration(platform, wrapper);
        try {
            Commandline cli = forkConfiguration.createCommandLine(startupConfiguration, 0, getReportsDirectory());
            System.out.println(cli);
        } catch (SurefireBooterForkException e) {
            throw new MojoExecutionException("Failed to create commandline for test", e);
        } finally {
            cleanupForkConfiguration(forkConfiguration);
        }
    }

    private StartupConfiguration createStartupConfiguration(DefaultScanResult scanResult, Platform platform,
                                                            Object resolvedJavaModularity)
            throws MojoExecutionException {
        Class<?> testClassPathClass = findClass("org.apache.maven.plugin.surefire.TestClassPath");
        Object testClassPath = invokeMethod(getMethod("generateTestClasspath"), Object.class);
        ProviderInfo provider = new EmptyProviderInfo();
        ClassLoaderConfiguration classLoaderConfiguration = getClassLoaderConfiguration();
        Class<?> resolvedPathClass = findClass("org.apache.maven.plugin.surefire.ResolvePathResultWrapper");
        Method method = getMethod("createStartupConfiguration", ProviderInfo.class, boolean.class,
                                  ClassLoaderConfiguration.class, DefaultScanResult.class, testClassPathClass,
                                  Platform.class, resolvedPathClass);
        return invokeMethod(method, StartupConfiguration.class, provider, true, classLoaderConfiguration, scanResult,
                            testClassPath, platform, resolvedJavaModularity);
    }

    private ForkConfiguration createForkConfiguration(Platform platform, Object resolvedJavaModularity)
            throws MojoExecutionException {
        Class<?> resolvedPathClass = findClass("org.apache.maven.plugin.surefire.ResolvePathResultWrapper");
        Method method = getMethod("createForkConfiguration", Platform.class, resolvedPathClass);
        return invokeMethod(method, ForkConfiguration.class, platform, resolvedJavaModularity);
    }

    private void forceForks() throws MojoExecutionException {
        setForkMode("once");
        setField("forkCount", "1");
        setField("reuseForks", true);
    }

    private void configure() throws MojoExecutionException {
        List<CommandLineOption> options = SurefireHelper.commandLineOptions(getSession(), getConsoleLogger());
        setField("cli", options);
        invokeMethod(getMethod("setupStuff"), void.class);
    }

    private Method getMethod(String methodName, Class<?>... parameterTypes) throws MojoExecutionException {
        try {
            Method method = AbstractSurefireMojo.class.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (ClassCastException | ReflectiveOperationException e) {
            throw new MojoExecutionException("Failed to find method " + methodName, e);
        }
    }

    private <T> T invokeMethod(Method method, Class<T> returnType, Object... args) throws MojoExecutionException {
        try {
            return returnType.cast(method.invoke(this, args));
        } catch (ClassCastException | ReflectiveOperationException e) {
            throw new MojoExecutionException("Failed to invoke method " + method, e);
        }
    }

    private void setField(String fieldName, Object value) throws MojoExecutionException {
        try {
            Field field = AbstractSurefireMojo.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            if (field.getType().equals(boolean.class)) {
                field.setBoolean(this, (Boolean) value);
            } else {
                field.set(this, value);
            }
        } catch (ReflectiveOperationException e) {
            throw new MojoExecutionException("Failed to set field " + fieldName, e);
        }
    }

    private Class<?> findClass(String className) throws MojoExecutionException {
        try {
            return Class.forName(className, true, getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Unable to find class: " + className, e);
        }
    }

    private static final class EmptyProviderInfo implements ProviderInfo {
        @Override
        @Nonnull
        public String getProviderName() {
            return "edu.neu.ccs.prl.pomelo.EmptyProvider";
        }

        @Override
        public boolean isApplicable() {
            return true;
        }

        @Override
        @Nonnull
        public Set<Artifact> getProviderClasspath() {
            return Collections.emptySet();
        }

        @Override
        public void addProviderProperties() {
        }

        @Nonnull
        public List<String[]> getJpmsArguments(@Nonnull ProviderRequirements forkRequirements) {
            return emptyList();
        }
    }
}
