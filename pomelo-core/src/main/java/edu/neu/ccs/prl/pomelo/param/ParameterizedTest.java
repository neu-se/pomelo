package edu.neu.ccs.prl.pomelo.param;

import com.pholser.junit.quickcheck.internal.ParameterTypeContext;
import edu.neu.ccs.prl.pomelo.fuzz.StructuredInputGenerator;
import edu.neu.ccs.prl.pomelo.fuzz.Fuzzer;

import java.util.List;

public interface ParameterizedTest {
    ParameterizedRunner createParameterizedRunner(Fuzzer supplier) throws Throwable;

    List<Object[]> getOriginalParameterGroups() throws Throwable;

    List<ParameterTypeContext> getParameterTypeContexts();

    String getDescriptor();

    default StructuredInputGenerator createGenerator() {
        return new StructuredInputGenerator(getParameterTypeContexts());
    }
}