package edu.neu.ccs.prl.pomelo;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public enum MavenTestPluginType {
    SUREFIRE("maven-surefire-plugin"),
    FAILSAFE("maven-failsafe-plugin");
    private static final String MAVEN_PLUGINS_GROUP_ID = "org.apache.maven.plugins";

    private final String artifactId;

    MavenTestPluginType(String artifactId) {
        this.artifactId = artifactId;
    }

    public List<Plugin> findMatches(MavenProject project) {
        return project.getBuildPlugins().stream().filter(p -> p.getGroupId().equals(MAVEN_PLUGINS_GROUP_ID))
                      .filter(p -> p.getArtifactId().equals(artifactId)).collect(Collectors.toList());
    }

    public Optional<PluginExecution> findExecution(MavenProject project, String id) {
        return findMatches(project)
                .stream()
                .map(p -> findExecution(p, id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private static Optional<PluginExecution> findExecution(Plugin plugin, String id) {
        return plugin.getExecutions()
                     .stream()
                     .filter(e -> e.getId().equals(id))
                     .findFirst();
    }
}
