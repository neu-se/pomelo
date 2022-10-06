package edu.neu.ccs.prl.pomelo;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import static edu.neu.ccs.prl.pomelo.PomeloLifecycleParticipant.*;

public final class ScanConfigurer {
    private final File report;

    public ScanConfigurer(MavenSession session) throws MavenExecutionException {
        this.report = initializeScanReport(session);
    }

    public void configure(MavenSession session) {
        addArtifactRepositories(session);
        session.getProjects().forEach(ScanConfigurer::addListenerDependency);
        reconfigureTestPluginExecutions(session, this::setScanReportValue);
        getAllTestPlugins(session).forEach(TestPluginType::removeUnsupportedGoals);
        reconfigureTestPluginExecutions(session, (e) -> prefixGoals(e, "scan-"));
        getAllTestPlugins(session).forEach(TestPluginType::replace);
    }

    private void setScanReportValue(PluginExecution execution) {
        addConfigurationValue(execution, "scanReport", report.getAbsolutePath());
    }

    private static File initializeScanReport(MavenSession session) throws MavenExecutionException {
        String path = session.getUserProperties().getProperty("pomelo.scan.report");
        File report = path == null ?
                new File(session.getTopLevelProject().getBuild().getDirectory(), "pomelo" + "-scan.txt") :
                new File(path);
        try {
            FileUtil.ensureNew(report);
            Files.write(report.toPath(), Collections.singletonList(ReportEntry.getCsvHeader()));
            return report;
        } catch (IOException e) {
            throw new MavenExecutionException("Failed to initialize scan report", e);
        }
    }

    private static void addListenerDependency(MavenProject project) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(POMELO_GROUP_ID);
        dependency.setArtifactId(POMELO_LISTENER_ARTIFACT_ID);
        dependency.setVersion(POMELO_VERSION);
        dependency.setScope("test");
        project.getDependencies().add(dependency);
    }
}
