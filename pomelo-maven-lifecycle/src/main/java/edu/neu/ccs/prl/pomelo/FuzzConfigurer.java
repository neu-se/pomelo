package edu.neu.ccs.prl.pomelo;

import edu.neu.ccs.prl.pomelo.scan.TestPluginType;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;

import static edu.neu.ccs.prl.pomelo.PomeloLifecycleParticipant.*;

public final class FuzzConfigurer {
    private final String projectId;
    private final String executionId;
    private final TestPluginType pluginType;

    public FuzzConfigurer(MavenSession session) throws MavenExecutionException {
        projectId = getRequiredProperty(session, "pomelo.project");
        executionId = getRequiredProperty(session, "pomelo.execution");
        pluginType = getTestPluginType(session, getRequiredProperty(session, "pomelo.plugin"));
        ensureProperty(session, "pomelo.testClass");
        ensureProperty(session, "pomelo.testMethod");
    }

    public void configure(MavenSession session) throws MavenExecutionException {
        addArtifactRepositories(session);
        replaceGoals(session, PomeloTask.FUZZ);
        filterExecutions(session);
        getAllTestPlugins(session).forEach(PomeloLifecycleParticipant::removeUnsupportedGoals);
        reconfigureTestPluginExecutions(session, (e) -> prefixGoals(e, "fuzz-"));
        getAllTestPlugins(session).forEach(PomeloLifecycleParticipant::replace);
    }

    private PluginExecution findSelectedExecution(MavenSession session) throws MavenExecutionException {
        for (MavenProject project : session.getProjects()) {
            if (project.getId().equals(projectId)) {
                for (Plugin plugin : project.getBuildPlugins()) {
                    if (pluginType.matches(plugin.getGroupId(), plugin.getArtifactId())) {
                        for (PluginExecution execution : plugin.getExecutions()) {
                            if (execution.getId().equals(executionId)) {
                                return execution;
                            }
                        }
                    }
                }
            }
        }
        throw new MavenExecutionException(
                String.format("Selected execution was not found: %s, %s, %s", projectId, pluginType, executionId),
                session.getRequest().getPom());
    }

    private void filterExecutions(MavenSession session) throws MavenExecutionException {
        // Set the phase of all test plugin executions not matching the selected execution to none
        PluginExecution selected = findSelectedExecution(session);
        String phase = selected.getPhase();
        PomeloLifecycleParticipant.reconfigureTestPluginExecutions(session, e -> e.setPhase("none"));
        selected.setPhase(phase);
    }

    public static TestPluginType getTestPluginType(MavenSession session, String name) throws MavenExecutionException {
        try {
            return TestPluginType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new MavenExecutionException("Invalid " + TestPluginType.class + " name: " + name,
                                              session.getRequest().getPom());
        }
    }
}
