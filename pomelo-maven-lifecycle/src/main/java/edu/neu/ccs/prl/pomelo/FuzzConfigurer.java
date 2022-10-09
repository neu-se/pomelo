package edu.neu.ccs.prl.pomelo;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;

import java.util.Collections;

import static edu.neu.ccs.prl.pomelo.PomeloLifecycleParticipant.*;

public final class FuzzConfigurer {
    private final String projectId;
    private final String executionId;
    private final TestPluginType pluginType;

    public FuzzConfigurer(MavenSession session) throws MavenExecutionException {
        projectId = getRequiredProperty(session, "pomelo.project");
        executionId = getRequiredProperty(session, "pomelo.execution");
        pluginType = TestPluginType.valueOf(session, getRequiredProperty(session, "pomelo.plugin"));
        getRequiredProperty(session, "pomelo.testClass");
        getRequiredProperty(session, "pomelo.testMethod");
    }

    public void configure(MavenSession session) {
        addArtifactRepositories(session);
        session.getProjects().forEach(PomeloLifecycleParticipant::addCoreDependency);
        filterExecutions(session);
        getAllTestPlugins(session).forEach(TestPluginType::removeUnsupportedGoals);
        reconfigureTestPluginExecutions(session, (e) -> prefixGoals(e, "fuzz-"));
        getAllTestPlugins(session).forEach(TestPluginType::replace);
    }


    private void filterExecutions(MavenProject project, Plugin plugin) {
        if (project.getId().equals(projectId) && pluginType.matches(plugin)) {
            plugin.getExecutions().removeIf(e -> !e.getId().equals(executionId));
        } else {
            plugin.setExecutions(Collections.emptyList());
        }
    }

    private void filterExecutions(MavenSession session) {
        for (MavenProject project : session.getProjects()) {
            getAllTestPlugins(project).forEach(p -> filterExecutions(project, p));
        }
    }
}
