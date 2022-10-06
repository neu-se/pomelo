package edu.neu.ccs.prl.pomelo;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public enum PomeloPhase {
    SCAN() {
        public void configure(MavenSession session) throws MavenExecutionException {
            File scanReport = initializeScanReport(session);
            addArtifactRepositories(session);
            session.getProjects().forEach(PomeloPhase::addListenerDependency);
            PomeloPhase.reconfigureTestPluginExecutions(session, (e) -> addScanReportValue(e, scanReport));
            PomeloPhase.replaceTestPlugins(session, "scan-");
        }

        private void addScanReportValue(PluginExecution execution, File scanReport) {
            Xpp3Dom configuration = (Xpp3Dom) execution.getConfiguration();
            if (configuration == null) {
                configuration = new Xpp3Dom("configuration");
                execution.setConfiguration(configuration);
            }
            Xpp3Dom node = new Xpp3Dom("pomeloScanReport");
            node.setValue(scanReport.getAbsolutePath());
            configuration.addChild(node);
        }

        private File initializeScanReport(MavenSession session) throws MavenExecutionException {
            String path = session.getUserProperties().getProperty("pomelo.scan.report");
            File report = path == null ?
                    new File(session.getTopLevelProject().getBuild().getDirectory(), "pomelo" + "-scan.txt") :
                    new File(path);
            try {
                FileUtil.ensureNew(report);
                Files.write(report.toPath(), Collections.singletonList(TestRecord.getCsvHeader()));
                return report;
            } catch (IOException e) {
                throw new MavenExecutionException("Failed to create pomelo.scan.report file",
                                                  session.getRequest().getPom());
            }
        }
    }, FUZZ() {
        public void configure(MavenSession session) {
            addArtifactRepositories(session);
            PomeloPhase.replaceTestPlugins(session, "fuzz-");
        }
    };
    static final String POMELO_VERSION = "1.0.0-SNAPSHOT";
    private static final String POMELO_GROUP_ID = "edu.neu.ccs.prl.pomelo";
    private static final String POMELO_LISTENER_ARTIFACT_ID = "pomelo-listener";
    private static final String SNAPSHOTS_REPO = "https://s01.oss.sonatype.org/content/repositories/snapshots";

    public abstract void configure(MavenSession session) throws MavenExecutionException;

    private static void replaceTestPlugins(MavenSession session, String prefix) {
        reconfigureTestPlugins(session, TestPluginType::removeUnsupportedGoals);
        reconfigureTestPluginExecutions(session, (e) -> prefixGoals(e, prefix));
        reconfigureTestPlugins(session, TestPluginType::replace);
    }

    private static void prefixGoals(PluginExecution execution, String prefix) {
        execution.setGoals(execution.getGoals()
                                    .stream()
                                    .map(g -> prefix + g)
                                    .collect(Collectors.toList()));
    }

    private static void addListenerDependency(MavenProject project) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(POMELO_GROUP_ID);
        dependency.setArtifactId(POMELO_LISTENER_ARTIFACT_ID);
        dependency.setVersion(POMELO_VERSION);
        dependency.setScope("test");
        project.getDependencies().add(dependency);
    }

    private static List<Plugin> getAllPlugins(MavenSession session) {
        return session.getProjects().stream().flatMap(p -> p.getBuildPlugins().stream())
                      .collect(Collectors.toList());
    }

    private static void reconfigureTestPlugins(MavenSession session, Consumer<Plugin> consumer) {
        getAllPlugins(session)
                .stream()
                .filter(TestPluginType::isTestPlugin)
                .forEach(consumer);
    }

    private static void reconfigureTestPluginExecutions(MavenSession session, Consumer<PluginExecution> consumer) {
        getAllPlugins(session)
                .stream()
                .filter(TestPluginType::isTestPlugin)
                .flatMap(p -> p.getExecutions().stream())
                .forEach(consumer);
    }

    private static void addArtifactRepositories(MavenSession session) {
        ArtifactRepositoryPolicy snapshotsPolicy =
                new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS,
                                             ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE);
        ArtifactRepositoryPolicy releasesPolicy =
                new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY,
                                             ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE);
        MavenArtifactRepository repository =
                new MavenArtifactRepository("pomelo.snapshots", SNAPSHOTS_REPO, new DefaultRepositoryLayout(),
                                            snapshotsPolicy, releasesPolicy);
        session.getRequest().addPluginArtifactRepository(repository);
        session.getRequest().addRemoteRepository(repository);
    }
}
