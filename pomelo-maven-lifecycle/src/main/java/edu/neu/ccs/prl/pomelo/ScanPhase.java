package edu.neu.ccs.prl.pomelo;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.File;
import java.io.IOException;

public enum ScanPhase {
    INSTANCE;

    public void configure(MavenSession session) throws MavenExecutionException {
        File scanReport = prepareScanReport(session);
        // Reconfigure Surefire and Failsafe
        for (MavenProject project : session.getProjects()) {
            addListenerDependency(project);
            for (MavenTestPluginType pluginType : MavenTestPluginType.values()) {
                for (Plugin plugin : pluginType.findMatches(project)) {
                    for (PluginExecution execution : plugin.getExecutions()) {
                        configure(project, execution, pluginType, scanReport);
                    }
                }
            }
        }

    }

    private File prepareScanReport(MavenSession session) throws MavenExecutionException {
        String path = session.getUserProperties().getProperty("pomelo.scan.report");
        File report;
        if (path == null) {
            File target = new File(session.getTopLevelProject().getBuild().getDirectory());
            report = new File(target, "pomelo-scan.txt");
        } else {
            report = new File(path);
        }
        System.out.println("Writing Pomelo scan report to: " + report);
        try {
            return FileUtil.ensureNew(report);
        } catch (IOException e) {
            throw new MavenExecutionException("Failed to prepare pomelo.scan.report file",
                                              session.getRequest().getPom());
        }
    }

    private void configure(MavenProject project, PluginExecution execution, MavenTestPluginType pluginType,
                           File scanReport) {
        Xpp3Dom configuration = (Xpp3Dom) execution.getConfiguration();
        if (configuration == null) {
            configuration = new Xpp3Dom("configuration");
            execution.setConfiguration(configuration);
        }
        setSystemProperties(project, execution, pluginType, scanReport, configuration);
        setProviderProperties(configuration);
    }

    private void setProviderProperties(Xpp3Dom configuration) {
        Xpp3Dom properties = ensureChild(configuration, "properties");
        Xpp3Dom property = new Xpp3Dom("property");
        property.addChild(makeNode("name", "listener"));
        property.addChild(makeNode("value", "edu.neu.ccs.prl.pomelo.PomeloJUnitListener"));
        properties.addChild(property);
    }

    private void setSystemProperties(MavenProject project, PluginExecution execution, MavenTestPluginType pluginType,
                                     File scanReport, Xpp3Dom configuration) {
        Xpp3Dom systemProperties = ensureChild(configuration, "systemProperties");
        systemProperties.addChild(makeNode("pomelo.scan.report", scanReport.getAbsolutePath()));
        systemProperties.addChild(makeNode("pomelo.scan.project", project.getFile().getAbsolutePath()));
        systemProperties.addChild(makeNode("pomelo.scan.plugin", pluginType.name()));
        systemProperties.addChild(makeNode("pomelo.scan.execution", execution.getId()));
    }

    private Xpp3Dom ensureChild(Xpp3Dom node, String name) {
        Xpp3Dom child = node.getChild(name);
        if (child == null) {
            child = new Xpp3Dom(name);
            node.addChild(child);
        }
        return child;
    }

    private Xpp3Dom makeNode(String name, String value) {
        Xpp3Dom result = new Xpp3Dom(name);
        result.setValue(value);
        return result;
    }

    private void addListenerDependency(MavenProject project) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(PomeloLifecycleParticipant.POMELO_GROUP_ID);
        dependency.setArtifactId(PomeloLifecycleParticipant.POMELO_LISTENER_ARTIFACT_ID);
        dependency.setVersion(PomeloLifecycleParticipant.POMELO_VERSION);
        dependency.setScope("test");
        project.getDependencies().add(dependency);
    }
}
