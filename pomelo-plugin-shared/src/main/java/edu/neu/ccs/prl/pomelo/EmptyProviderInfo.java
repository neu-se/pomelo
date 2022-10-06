package edu.neu.ccs.prl.pomelo;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.surefire.providerapi.ProviderInfo;
import org.apache.maven.surefire.providerapi.ProviderRequirements;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;

public final class EmptyProviderInfo implements ProviderInfo {
    @Override
    @Nonnull
    public String getProviderName() {
        return "EmptyProvider";
    }

    @Override
    public boolean isApplicable() {
        return true;
    }

    @Override
    @Nonnull
    public Set<Artifact> getProviderClasspath() {
        return Collections.emptySet();
    }

    @Override
    public void addProviderProperties() {
    }

    @Nonnull
    public List<String[]> getJpmsArguments(@Nonnull ProviderRequirements forkRequirements) {
        return emptyList();
    }
}
