package edu.neu.ccs.prl.pomelo;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;

import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "pomelo")
public class PomeloLifecycleParticipant extends AbstractMavenLifecycleParticipant {
    static final String SNAPSHOTS_REPO = "https://s01.oss.sonatype.org/content/repositories/snapshots";

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        String task = session.getUserProperties().getProperty("pomelo.task");
        if (task != null) {
            PomeloTask.valueOf(session, task).configure(session);
        }
    }

    static void ensureProperty(MavenSession session, String key) throws MavenExecutionException {
        String value = session.getUserProperties().getProperty(key);
        if (value == null) {
            throw new MavenExecutionException("Invalid missing property: " + key, session.getRequest().getPom());
        }
    }

    static String getRequiredProperty(MavenSession session, String key) throws MavenExecutionException {
        String value = session.getUserProperties().getProperty(key);
        if (value == null) {
            throw new MavenExecutionException("Invalid missing property: " + key, session.getRequest().getPom());
        }
        return value;
    }

    static void prefixGoals(PluginExecution execution, String prefix) {
        execution.setGoals(execution.getGoals().stream().map(g -> prefix + g).collect(Collectors.toList()));
    }

    static void addArtifactRepositories(MavenSession session) {
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

    static java.util.stream.Stream<Plugin> getAllTestPlugins(MavenProject project) {
        return project.getBuildPlugins().stream().filter(TestPluginType::isTestPlugin);
    }

    static java.util.stream.Stream<Plugin> getAllTestPlugins(MavenSession session) {
        return session.getProjects().stream().flatMap(PomeloLifecycleParticipant::getAllTestPlugins);
    }

    static void reconfigureTestPluginExecutions(MavenSession session, Consumer<PluginExecution> consumer) {
        getAllTestPlugins(session).flatMap(p -> p.getExecutions().stream()).forEach(consumer);
    }
}
