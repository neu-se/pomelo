package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.meringue.FileUtil;
import edu.neu.ccs.prl.meringue.JvmLauncher;
import edu.neu.ccs.prl.pomelo.scan.ScanForkMain;
import edu.neu.ccs.prl.pomelo.util.SystemPropertyUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.AbstractSurefireMojo;
import org.apache.maven.plugin.surefire.JdkAttributes;
import org.apache.maven.plugin.surefire.SurefireProperties;
import org.apache.maven.plugin.surefire.booterclient.lazytestprovider.Commandline;
import org.apache.maven.surefire.booter.Classpath;
import org.apache.maven.surefire.shared.utils.cli.CommandLineException;
import org.apache.maven.surefire.shared.utils.cli.CommandLineUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.maven.plugin.surefire.SurefireHelper.replaceForkThreadsInPath;
import static org.apache.maven.plugin.surefire.SurefireHelper.replaceThreadNumberPlaceholders;

public class JvmConfiguration {
    private final boolean debug;
    private final boolean enableAssertions;
    private final File rawWorkingDirectory;
    private final Map<String, String> environment;
    private final String rawArgLine;
    private final File javaExecutable;
    private final SurefireProperties rawSystemProperties;
    private final List<File> testClasspathElements;

    public JvmConfiguration(boolean debug, boolean enableAssertions, File workingDirectory, Properties modelProperties,
                            String argLine, Map<String, String> environmentVariables,
                            String[] excludedEnvironmentVariables, JdkAttributes jdkAttributes, Classpath testClasspath,
                            SurefireProperties rawSystemProperties) {
        this.debug = debug;
        this.enableAssertions = enableAssertions;
        this.rawWorkingDirectory = workingDirectory;
        this.rawSystemProperties = rawSystemProperties;
        this.rawArgLine = interpolatePropertyExpressions(argLine, modelProperties).replaceAll("\\s", " ");
        this.javaExecutable = jdkAttributes.getJvmExecutable();
        this.testClasspathElements = Collections.unmodifiableList(
                testClasspath.getClassPath().stream().map(File::new).collect(Collectors.toList()));
        this.environment =
                Collections.unmodifiableMap(createEnvironment(environmentVariables, excludedEnvironmentVariables));
    }

    public boolean isDebug() {
        return debug;
    }

    public List<String> getJavaOptions(int forkNumber) throws MojoExecutionException {
        try {
            List<String> options =
                    new ArrayList<>(Arrays.asList(CommandLineUtils.translateCommandline(getArgLine(forkNumber))));
            if (enableAssertions) {
                options.add("-ea");
            }
            return options;
        } catch (CommandLineException e) {
            throw new MojoExecutionException("Failed to extract Java options", e);
        }
    }

    public File getJavaExecutable() {
        return javaExecutable;
    }

    public List<File> getTestClasspathElements() {
        return testClasspathElements;
    }

    public Properties getSystemProperties(int forkNumber) {
        return AbstractSurefireMojo.createCopyAndReplaceForkNumPlaceholder(rawSystemProperties, forkNumber);
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public String getArgLine(int forkNumber) {
        return replaceThreadNumberPlaceholders(rawArgLine, forkNumber);
    }

    public File getWorkingDirectory(int forkNumber) throws MojoExecutionException {
        File cwd = replaceForkThreadsInPath(rawWorkingDirectory, forkNumber);
        try {
            FileUtil.ensureDirectory(cwd);
            return cwd;
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create workingDirectory " + cwd.getAbsolutePath(), e);
        }
    }

    public File buildManifestJar(File temporaryDirectory, Collection<File> additionalClasspathElements)
            throws MojoExecutionException {
        File manifestJar = new File(temporaryDirectory, "pomelo-test.jar");
        List<File> elements = new ArrayList<>(testClasspathElements);
        elements.addAll(additionalClasspathElements);
        try {
            FileUtil.buildManifestJar(elements, manifestJar);
            return manifestJar;
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to build test classpath manifest JAR", e);
        }
    }

    public File writeSystemProperties(File temporaryDirectory, int forkNumber) throws MojoExecutionException {
        File file = new File(temporaryDirectory, "pomelo" + forkNumber + ".properties");
        try {
            PluginUtil.ensureNew(file);
            SystemPropertyUtil.store(file, null, getSystemProperties(forkNumber));
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write properties to file: " + file, e);
        }
        return file;
    }

    public JvmLauncher createLauncher(File temporaryDirectory, int forkNumber,
                                      Collection<File> additionalClasspathElements, boolean verbose)
            throws MojoExecutionException {
        File manifestJar = buildManifestJar(temporaryDirectory, additionalClasspathElements);
        List<String> options = getJavaOptions(forkNumber);
        if (debug) {
            options.add(JvmLauncher.DEBUG_OPT + "5005");
        }
        options.add("-cp");
        options.add(manifestJar.getAbsolutePath());
        return JvmLauncher.fromMain(javaExecutable, ScanForkMain.class.getName(), options.toArray(new String[0]),
                                    verbose || debug, new String[0], getWorkingDirectory(forkNumber), environment);
    }

    private static String interpolatePropertyExpressions(String argLine, Properties modelProperties) {
        if (argLine == null) {
            return "";
        }
        String result = argLine.trim();
        if (result.isEmpty()) {
            return "";
        }
        for (String name : modelProperties.stringPropertyNames()) {
            String expression = "@{" + name + "}";
            if (argLine.contains(expression)) {
                result = result.replace(expression, modelProperties.getProperty(name, ""));
            }
        }
        return result;
    }

    private static Map<String, String> createEnvironment(Map<String, String> environmentVariables,
                                                         String[] excludedEnvironmentVariables) {
        Commandline cli = new Commandline(excludedEnvironmentVariables);
        for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
            String value = entry.getValue();
            cli.addEnvironment(entry.getKey(), value == null ? "" : value);
        }
        Map<String, String> map = new HashMap<>();
        for (String entry : cli.getEnvironmentVariables()) {
            int index = entry.indexOf('=');
            if (index != -1) {
                map.put(entry.substring(0, index), entry.substring(index + 1));
            }
        }
        return map;
    }
}
