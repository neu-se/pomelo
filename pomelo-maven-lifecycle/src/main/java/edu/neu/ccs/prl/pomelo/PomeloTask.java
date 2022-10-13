package edu.neu.ccs.prl.pomelo;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;

public enum PomeloTask {
    SCAN() {
        public void configure(MavenSession session) throws MavenExecutionException {
            new ScanConfigurer().configure(session);
        }
    }, FUZZ() {
        public void configure(MavenSession session) throws MavenExecutionException {
            new FuzzConfigurer(session).configure(session);
        }
    };

    public abstract void configure(MavenSession session) throws MavenExecutionException;

    public static PomeloTask valueOf(MavenSession session, String name) throws MavenExecutionException {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new MavenExecutionException("Invalid task: " + name, session.getRequest().getPom());
        }
    }
}
