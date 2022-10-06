package edu.neu.ccs.prl.pomelo;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "pomelo")
public class PomeloLifecycleParticipant extends AbstractMavenLifecycleParticipant {


    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        String phaseName = session.getUserProperties().getProperty("pomelo.phase");
        if (phaseName != null) {
            try {
                PomeloPhase phase = PomeloPhase.valueOf(phaseName.toUpperCase());
                phase.configure(session);
            } catch (IllegalArgumentException e) {
                throw new MavenExecutionException("Invalid pomelo.phase value: " + phaseName,
                                                  session.getRequest().getPom());
            }
        }
    }
}
