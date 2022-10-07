package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.pomelo.scan.ReportEntry;
import edu.neu.ccs.prl.pomelo.util.FileUtil;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.PluginExecution;

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
        session.getProjects().forEach(PomeloLifecycleParticipant::addCoreDependency);
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
}
