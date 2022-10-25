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
        replaceGoals(session, PomeloTask.SCAN);
        addArtifactRepositories(session);
        getAllTestPlugins(session).forEach(PomeloLifecycleParticipant::removeUnsupportedGoals);
        reconfigureTestPluginExecutions(session, (e) -> prefixGoals(e, "scan-"));
        getAllTestPlugins(session).forEach(PomeloLifecycleParticipant::replace);
    }

    private static void initializeScanReport(MavenSession session) throws MavenExecutionException {
        String path = session.getUserProperties().getProperty("pomelo.report");
        File report = path == null ?
                new File(session.getTopLevelProject().getBuild().getDirectory(), "pomelo" + "-scan.csv") :
                new File(path);
        try {
            FileUtil.ensureNew(report);
            Files.write(report.toPath(), Collections.singletonList(ReportEntry.getCsvHeader()));
            session.getUserProperties().put("pomelo.report.internal", report.getAbsolutePath());
        } catch (IOException e) {
            throw new MavenExecutionException("Failed to initialize scan report", e);
        }
    }
}
