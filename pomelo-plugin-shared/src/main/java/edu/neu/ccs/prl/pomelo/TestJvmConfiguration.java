package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.meringue.FileUtil;
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

import static org.apache.maven.plugin.surefire.SurefireHelper.replaceForkThreadsInPath;
import static org.apache.maven.plugin.surefire.SurefireHelper.replaceThreadNumberPlaceholders;

public class TestJvmConfiguration {
    private final boolean debug;
    private final boolean enableAssertions;
    private final File rawWorkingDirectory;
    private final Map<String, String> environment;
    private final String rawArgLine;
    private final File javaExecutable;
    private final Classpath testClasspath;
    private final SurefireProperties rawSystemProperties;

    public TestJvmConfiguration(boolean debug, boolean enableAssertions, File workingDirectory,
                                Properties modelProperties, String argLine, Map<String, String> environmentVariables,
                                String[] excludedEnvironmentVariables, JdkAttributes jdkAttributes,
                                Classpath testClasspath, SurefireProperties rawSystemProperties) {
        this.debug = debug;
        this.enableAssertions = enableAssertions;
        this.rawWorkingDirectory = workingDirectory;
        this.rawSystemProperties = rawSystemProperties;
        this.rawArgLine = interpolatePropertyExpressions(argLine, modelProperties).replaceAll("\\s", " ");
        this.javaExecutable = jdkAttributes.getJvmExecutable();
        this.testClasspath = testClasspath;
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

    public boolean isEnableAssertions() {
        return enableAssertions;
    }

    public File getJavaExecutable() {
        return javaExecutable;
    }

    public Classpath getTestClasspath() {
        return testClasspath;
    }

    public List<File> getTestClassPathElements() {
        List<File> result = new ArrayList<>();
        for (String s : testClasspath) {
            result.add(new File(s));
        }
        return result;
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
        return convertEnvironment(cli.getEnvironmentVariables());
    }

    private static Map<String, String> convertEnvironment(String[] environment) {
        Map<String, String> map = new HashMap<>();
        for (String entry : environment) {
            int index = entry.indexOf('=');
            if (index != -1) {
                map.put(entry.substring(0, index), entry.substring(index + 1));
            }
        }
        return map;
    }
}
