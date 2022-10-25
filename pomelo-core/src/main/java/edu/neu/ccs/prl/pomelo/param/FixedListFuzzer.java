package edu.neu.ccs.prl.pomelo.param;

import edu.neu.ccs.prl.pomelo.fuzz.Fuzzer;
import edu.neu.ccs.prl.pomelo.fuzz.StructuredFuzzTarget;

import java.util.Collection;
import java.util.LinkedList;

public class FixedListFuzzer implements Fuzzer {
    private final LinkedList<Object[]> groups;

    public FixedListFuzzer(Collection<Object[]> groups) {
        this.groups = new LinkedList<>(groups);
    }

    @Override
    public void accept(StructuredFuzzTarget target) throws Throwable {
        while (!groups.isEmpty()) {
            target.run(groups.poll());
        }
    }
}
