package edu.neu.ccs.prl.pomelo;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum MavenTestPluginType {
    SUREFIRE("maven-surefire-plugin", "pomelo-surefire-maven-plugin", "test"),
    FAILSAFE("maven-failsafe-plugin", "pomelo-failsafe-maven-plugin", "integration-test");
    private static final String MAVEN_PLUGINS_GROUP_ID = "org.apache.maven.plugins";
    private static final String POMELO_PLUGINS_GROUP_ID = "edu.neu.ccs.prl.pomelo";

    private final String mavenArtifactId;
    private final String pomeloArtifactId;
    private final Set<String> supportedGoals;

    MavenTestPluginType(String mavenArtifactId, String pomeloArtifactId, String... supportedGoals) {
        this.mavenArtifactId = mavenArtifactId;
        this.pomeloArtifactId = pomeloArtifactId;
        this.supportedGoals = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(supportedGoals)));
    }

    public boolean matches(Plugin plugin) {
        return MAVEN_PLUGINS_GROUP_ID.equals(plugin.getGroupId()) && mavenArtifactId.equals(plugin.getArtifactId());
    }

    private void removeUnsupportedGoals(PluginExecution execution) {
        execution.getGoals().retainAll(supportedGoals);
    }

    public static boolean isTestPlugin(Plugin plugin) {
        for (MavenTestPluginType type : values()) {
            if (type.matches(plugin)) {
                return true;
            }
        }
        return false;
    }

    public static void replace(Plugin plugin) {
        for (MavenTestPluginType type : values()) {
            if (type.matches(plugin)) {
                plugin.setGroupId(POMELO_PLUGINS_GROUP_ID);
                plugin.setArtifactId(type.pomeloArtifactId);
                plugin.setVersion(PomeloPhase.POMELO_VERSION);
                return;
            }
        }
    }

    public static void removeUnsupportedGoals(Plugin plugin) {
        for (MavenTestPluginType type : values()) {
            if (type.matches(plugin)) {
                plugin.getExecutions()
                      .forEach(type::removeUnsupportedGoals);
                return;
            }
        }
    }
}
