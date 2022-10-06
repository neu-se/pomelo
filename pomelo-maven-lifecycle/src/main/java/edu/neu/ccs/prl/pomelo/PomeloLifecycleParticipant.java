package edu.neu.ccs.prl.pomelo;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "pomelo")
public class PomeloLifecycleParticipant extends AbstractMavenLifecycleParticipant {
    static final String POMELO_GROUP_ID = "edu.neu.ccs.prl.pomelo";
    static final String POMELO_LISTENER_ARTIFACT_ID = "pomelo-listener";
    static final String POMELO_PLUGIN_ARTIFACT_ID = "pomelo-maven-plugin";
    static final String POMELO_VERSION = "1.0.0-SNAPSHOT";
    static final String SNAPSHOT_REPO = "https://s01.oss.sonatype.org/content/repositories/snapshots";

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        String phase = session.getUserProperties().getProperty("pomelo.phase");
        if (phase != null) {
            switch (phase) {
                case "scan":
                    ScanPhase.INSTANCE.configure(session);
                    break;
                case "fuzz":
                    configureForFuzz(session);
                    break;
                case "analyze":
                    break;
                default:
                    throw new MavenExecutionException("Invalid pomelo.phase value: " + phase,
                                                      session.getRequest().getPom());
            }
        }
    }

    private void configureForFuzz(MavenSession session) {
        addPluginRepo(session);
        // Replace Surefire and Failsafe with Pomelo
        for (MavenProject project : session.getProjects()) {
            for (Plugin plugin : MavenTestPluginType.SUREFIRE.findMatches(project)) {
                plugin.setGroupId(POMELO_GROUP_ID);
                plugin.setArtifactId(POMELO_PLUGIN_ARTIFACT_ID);
                plugin.setVersion(POMELO_VERSION);
            }
            for (Plugin plugin : MavenTestPluginType.FAILSAFE.findMatches(project)) {
                plugin.setGroupId(POMELO_GROUP_ID);
                plugin.setArtifactId(POMELO_PLUGIN_ARTIFACT_ID);
                plugin.setVersion(POMELO_VERSION);
            }
        }
    }

    private void addPluginRepo(MavenSession session) {
        ArtifactRepositoryPolicy snapshotsPolicy =
                new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS,
                                             ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE);
        ArtifactRepositoryPolicy releasesPolicy =
                new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY,
                                             ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE);
        session.getRequest().getPluginArtifactRepositories()
               .add(new MavenArtifactRepository("pomelo.snapshots", SNAPSHOT_REPO, new DefaultRepositoryLayout(),
                                                snapshotsPolicy, releasesPolicy));
    }
}
