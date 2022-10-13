package edu.neu.ccs.prl.pomelo;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;

import java.util.*;

public enum TestPluginType {
    SUREFIRE("maven-surefire-plugin", "pomelo-surefire-maven-plugin", "surefire", "test"),
    FAILSAFE("maven-failsafe-plugin", "pomelo-failsafe-maven-plugin", "failsafe", "integration-test");
    private static final String MAVEN_PLUGINS_GROUP_ID = "org.apache.maven.plugins";
    private static final String POMELO_PLUGINS_GROUP_ID = "edu.neu.ccs.prl.pomelo";

    private final String mavenArtifactId;
    private final String pomeloArtifactId;
    private final String pluginPrefix;
    private final Set<String> supportedGoals;

    TestPluginType(String mavenArtifactId, String pomeloArtifactId, String pluginPrefix, String... supportedGoals) {
        this.mavenArtifactId = mavenArtifactId;
        this.pomeloArtifactId = pomeloArtifactId;
        this.pluginPrefix = pluginPrefix;
        this.supportedGoals = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(supportedGoals)));
    }

    public boolean matches(String groupId, String artifactId) {
        return MAVEN_PLUGINS_GROUP_ID.equals(groupId) && mavenArtifactId.equals(artifactId);
    }

    public boolean matches(Plugin plugin) {
        return matches(plugin.getGroupId(), plugin.getArtifactId());
    }

    private void removeUnsupportedGoals(PluginExecution execution) {
        execution.getGoals().retainAll(supportedGoals);
    }

    public static boolean isTestPlugin(Plugin plugin) {
        for (TestPluginType type : values()) {
            if (type.matches(plugin)) {
                return true;
            }
        }
        return false;
    }

    public static void replace(Plugin plugin) {
        for (TestPluginType type : values()) {
            if (type.matches(plugin)) {
                plugin.setGroupId(POMELO_PLUGINS_GROUP_ID);
                plugin.setArtifactId(type.pomeloArtifactId);
                plugin.setVersion(PomeloLifecycleParticipant.POMELO_VERSION);
                return;
            }
        }
    }

    public static void removeUnsupportedGoals(Plugin plugin) {
        for (TestPluginType type : values()) {
            if (type.matches(plugin)) {
                plugin.getExecutions().forEach(type::removeUnsupportedGoals);
                return;
            }
        }
    }

    public static TestPluginType valueOf(MavenSession session, String name) throws MavenExecutionException {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new MavenExecutionException("Invalid " + TestPluginType.class + " name: " + name,
                                              session.getRequest().getPom());
        }
    }

    private static String createGoalString(TestPluginType type, String goal, String executionId, PomeloTask phase) {
        String newGoal = String.format("%s-%s", phase.name().toLowerCase(), goal);
        String s = String.format("%s:%s:%s:%s", POMELO_PLUGINS_GROUP_ID, type.pomeloArtifactId,
                                 PomeloLifecycleParticipant.POMELO_VERSION, newGoal);
        return executionId == null ? s : s + "@" + executionId;
    }

    private static String getExecutionId(String pluginGoal) {
        int executionIdx = pluginGoal.indexOf('@');
        if (executionIdx > 0) {
            return pluginGoal.substring(executionIdx + 1);
        }
        return null;
    }

    private static TestPluginType findMatch(String groupId, String artifactId) {
        for (TestPluginType type : values()) {
            if (type.matches(groupId, artifactId)) {
                return type;
            }
        }
        return null;
    }

    private static TestPluginType findMatch(String pluginPrefix) {
        for (TestPluginType type : values()) {
            if (type.pluginPrefix.equals(pluginPrefix)) {
                return type;
            }
        }
        return null;
    }

    private static String remapGoal(final String pluginGoal, PomeloTask phase) {
        // pluginPrefix:goal[@execution] or groupId:artifactId[:version]:goal[@execution]
        String executionId = getExecutionId(pluginGoal);
        String goalStart = pluginGoal;
        if (executionId != null) {
            goalStart = pluginGoal.substring(0, pluginGoal.length() - 1 - executionId.length());
        }
        String[] parts = goalStart.split(":");
        if (parts.length < 2) {
            return pluginGoal;
        }
        String goal = parts[parts.length - 1];
        TestPluginType type;
        if (parts.length == 2) {
            type = findMatch(parts[0]);
        } else {
            type = findMatch(parts[0], parts[1]);
        }
        if (type == null) {
            return pluginGoal;
        }
        return type.supportedGoals.contains(goal) ? createGoalString(type, goal, executionId, phase) : null;
    }

    public static void replaceGoals(MavenSession session, PomeloTask phase) {
        List<String> result = new ArrayList<>();
        for (String goal : session.getGoals()) {
            String g = remapGoal(goal, phase);
            if (g != null) {
                result.add(g);
            }
        }
        session.getRequest().setGoals(result);
    }
}
