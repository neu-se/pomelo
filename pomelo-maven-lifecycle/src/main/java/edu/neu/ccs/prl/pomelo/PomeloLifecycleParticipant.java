package edu.neu.ccs.prl.pomelo;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "pomelo")
public class PomeloLifecycleParticipant extends AbstractMavenLifecycleParticipant {
    private static final String POMELO_PLUGIN_GROUP_ID = "edu.neu.ccs.prl.pomelo";
    private static final String POMELO_PLUGIN_ARTIFACT_ID = "pomelo-maven-plugin";
    private static final String POMELO_PLUGIN_VERSION = "1.0.0-SNAPSHOT";
    private static final String SNAPSHOT_REPO = "https://s01.oss.sonatype.org/content/repositories/snapshots";


    @Override
    public void afterProjectsRead(MavenSession session) {
        addPluginRepo(session);
        // Replace Surefire and Failsafe with Pomelo
        for (MavenProject project : session.getProjects()) {
            for (Plugin plugin : MavenTestPlugin.SUREFIRE.findMatches(project)) {
                plugin.setGroupId(POMELO_PLUGIN_GROUP_ID);
                plugin.setArtifactId(POMELO_PLUGIN_ARTIFACT_ID);
                plugin.setVersion(POMELO_PLUGIN_VERSION);
            }
            for (Plugin plugin : MavenTestPlugin.FAILSAFE.findMatches(project)) {
                plugin.setGroupId(POMELO_PLUGIN_GROUP_ID);
                plugin.setArtifactId(POMELO_PLUGIN_ARTIFACT_ID);
                plugin.setVersion(POMELO_PLUGIN_VERSION);
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
