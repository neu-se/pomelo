package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.pomelo.scan.TestPluginType;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.util.List;

public interface ScanningMojo {
    MojoExecution getMojoExecution();

    File getReport();

    File getTemporaryDirectory();

    int getTimeout();

    boolean isVerbose();

    void executeSuper() throws MojoExecutionException, MojoFailureException;

    TestPluginType getOriginalPluginType();

    Log getLog();

    List<File> getCoreArtifactClasspath() throws MojoExecutionException;
}