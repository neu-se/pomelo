package edu.neu.ccs.prl.pomelo.param;

public interface ParameterSupplier {
    boolean hasNext();

    Object[] next();
}