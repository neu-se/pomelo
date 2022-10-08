package edu.neu.ccs.prl.pomelo.test;

import java.util.List;

public interface ParameterizedTestWrapper {
    ParameterizedRunner createParameterizedRunner(ParameterSupplier supplier) throws Throwable;

    List<Object[]> getOriginalParameterGroups() throws Throwable;
}