package edu.neu.ccs.prl.pomelo.param;

import java.util.Collection;
import java.util.LinkedList;

public class ListParameterSupplier implements ParameterSupplier {
    private final LinkedList<Object[]> parameterGroups;

    public ListParameterSupplier(Collection<Object[]> parametersSets) {
        this.parameterGroups = new LinkedList<>(parametersSets);
    }

    @Override
    public boolean hasNext() {
        return !parameterGroups.isEmpty();
    }

    @Override
    public Object[] next() {
        return parameterGroups.poll();
    }
}
