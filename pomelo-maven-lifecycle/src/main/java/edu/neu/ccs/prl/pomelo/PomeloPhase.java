package edu.neu.ccs.prl.pomelo;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;

public enum PomeloPhase {
    SCAN() {
        public void configure(MavenSession session) throws MavenExecutionException {
            new ScanConfigurer(session).configure(session);
        }
    }, FUZZ() {
        public void configure(MavenSession session) throws MavenExecutionException {
            new FuzzConfigurer(session).configure(session);
        }
    };

    public abstract void configure(MavenSession session) throws MavenExecutionException;

    public static PomeloPhase valueOf(MavenSession session, String name) throws MavenExecutionException {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new MavenExecutionException("Invalid " + PomeloPhase.class + " name: " + name,
                                              session.getRequest().getPom());
        }
    }
}
