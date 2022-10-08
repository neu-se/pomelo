package edu.neu.ccs.prl.pomelo.test;

public interface ParameterSupplier {
    boolean hasNext();

    Object[] next();
}