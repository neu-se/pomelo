package edu.neu.ccs.prl.pomelo.param;

import com.pholser.junit.quickcheck.internal.ParameterTypeContext;

import java.util.List;

public interface ParameterizedTestWrapper {
    ParameterizedRunner createParameterizedRunner(ParameterSupplier supplier) throws Throwable;

    List<Object[]> getOriginalParameterGroups() throws Throwable;

    List<ParameterTypeContext> getParameterTypeContexts();
}