package edu.neu.ccs.prl.pomelo.scan;

public enum TestPluginType {
    SUREFIRE("maven-surefire-plugin", "surefire", "test"),
    FAILSAFE("maven-failsafe-plugin", "failsafe", "integration-test");
    private static final String MAVEN_PLUGINS_GROUP_ID = "org.apache.maven.plugins";

    private final String mavenArtifactId;
    private final String pluginPrefix;
    private final String supportedGoal;

    TestPluginType(String mavenArtifactId, String pluginPrefix, String supportedGoal) {
        this.mavenArtifactId = mavenArtifactId;
        this.pluginPrefix = pluginPrefix;
        this.supportedGoal = supportedGoal;
    }

    public boolean isSupportedGoal(String goal) {
        return supportedGoal.equals(goal);
    }

    public boolean matches(String groupId, String artifactId) {
        return MAVEN_PLUGINS_GROUP_ID.equals(groupId) && mavenArtifactId.equals(artifactId);
    }

    public boolean ownsSessionGoal(String sessionGoal) {
        // pluginPrefix:goal[@execution] or groupId:artifactId[:version]:goal[@execution]
        String[] parts = sessionGoal.split(":");
        if (parts.length == 2) {
            return pluginPrefix.equals(parts[0]);
        } else if (parts.length > 2) {
            return matches(parts[0], parts[1]);
        } else {
            return false;
        }
    }

    public static TestPluginType findMatch(String groupId, String artifactId) {
        for (TestPluginType type : values()) {
            if (type.matches(groupId, artifactId)) {
                return type;
            }
        }
        return null;
    }

    public static TestPluginType findSessionGoalOwner(String sessionGoal) {
        for (TestPluginType type : values()) {
            if (type.ownsSessionGoal(sessionGoal)) {
                return type;
            }
        }
        return null;
    }
}