package edu.neu.ccs.prl.pomelo;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static edu.neu.ccs.prl.pomelo.PomeloLifecycleParticipant.*;

public final class FuzzConfigurer {
    private final String projectId;
    private final String executionId;
    private final TestPluginType pluginType;

    public FuzzConfigurer(MavenSession session) throws MavenExecutionException {
        projectId = getRequiredProperty(session, "pomelo.project");
        executionId = getRequiredProperty(session, "pomelo.execution");
        pluginType = TestPluginType.valueOf(session, getRequiredProperty(session, "pomelo.plugin"));
        ensureProperty(session, "pomelo.testClass");
        ensureProperty(session, "pomelo.testMethod");
    }

    public void configure(MavenSession session) throws MavenExecutionException {
        addArtifactRepositories(session);
        TestPluginType.replaceGoals(session, PomeloTask.FUZZ);
        filterExecutions(session);
        getAllTestPlugins(session).forEach(TestPluginType::removeUnsupportedGoals);
        reconfigureTestPluginExecutions(session, (e) -> prefixGoals(e, "fuzz-"));
        getAllTestPlugins(session).forEach(TestPluginType::replace);
    }

    private void filterExecutions(MavenSession session) throws MavenExecutionException {
        // Remove all test plugin executions not matching the selected execution
        boolean foundMatch = false;
        for (MavenProject project : session.getProjects()) {
            for (Plugin plugin : getAllTestPlugins(project).collect(Collectors.toList())) {
                List<PluginExecution> executions = plugin.getExecutions();
                for (Iterator<PluginExecution> itr = executions.iterator(); itr.hasNext(); ) {
                    PluginExecution execution = itr.next();
                    if (project.getId().equals(projectId) && pluginType.matches(plugin) &&
                            execution.getId().equals(executionId)) {
                        if (foundMatch) {
                            throw new MavenExecutionException(
                                    String.format("Found more than one execution matching: %s, %s, %s", projectId,
                                                  pluginType, executionId), session.getRequest().getPom());
                        }
                        foundMatch = true;
                    } else {
                        itr.remove();
                    }
                }
            }

        }
        if (!foundMatch) {
            throw new MavenExecutionException(
                    String.format("Selected execution was not found: %s, %s, %s", projectId, pluginType, executionId),
                    session.getRequest().getPom());
        }
    }
}
