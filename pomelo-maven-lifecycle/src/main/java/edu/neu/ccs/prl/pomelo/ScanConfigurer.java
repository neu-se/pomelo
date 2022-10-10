package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.pomelo.scan.ReportEntry;
import edu.neu.ccs.prl.pomelo.util.FileUtil;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import static edu.neu.ccs.prl.pomelo.PomeloLifecycleParticipant.*;

public final class ScanConfigurer {
    public void configure(MavenSession session) throws MavenExecutionException {
        initializeScanReport(session);
        addArtifactRepositories(session);
        session.getProjects().forEach(PomeloLifecycleParticipant::addCoreDependency);
        getAllTestPlugins(session).forEach(TestPluginType::removeUnsupportedGoals);
        reconfigureTestPluginExecutions(session, (e) -> prefixGoals(e, "scan-"));
        getAllTestPlugins(session).forEach(TestPluginType::replace);
    }

    private static void initializeScanReport(MavenSession session) throws MavenExecutionException {
        File report = new File(PomeloLifecycleParticipant.getRequiredProperty(session, "pomelo.scan.report"));
        try {
            FileUtil.ensureNew(report);
            Files.write(report.toPath(), Collections.singletonList(ReportEntry.getCsvHeader()));
        } catch (IOException e) {
            throw new MavenExecutionException("Failed to initialize scan report", e);
        }
    }
}
