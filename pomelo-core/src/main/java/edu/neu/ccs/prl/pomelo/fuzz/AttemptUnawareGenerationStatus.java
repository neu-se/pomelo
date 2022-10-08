package edu.neu.ccs.prl.pomelo.fuzz;

import com.pholser.junit.quickcheck.internal.GeometricDistribution;
import com.pholser.junit.quickcheck.internal.generator.SimpleGenerationStatus;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

public class AttemptUnawareGenerationStatus extends SimpleGenerationStatus {
    private static final int MEAN_SIZE = 10;
    private final SourceOfRandomness random;
    private final GeometricDistribution distro;

    public AttemptUnawareGenerationStatus(SourceOfRandomness random) {
        this(random, new GeometricDistribution());
    }

    private AttemptUnawareGenerationStatus(SourceOfRandomness random, GeometricDistribution distro) {
        super(distro, random, 0);
        this.distro = distro;
        this.random = random;
    }

    @Override
    public int size() {
        return distro.sampleWithMean(MEAN_SIZE, random);
    }

    @Override
    public int attempts() {
        throw new UnsupportedOperationException();
    }
}
