package edu.neu.ccs.prl.pomelo;

import junitparams.Parameters;
import org.junit.Assume;
import org.junit.Test;

public class ChildTest extends MethodParamTest {
    @Test
    @Parameters({"false"})
    @Override
    public void test(boolean param2) {
        Assume.assumeFalse(param2);
    }
}
