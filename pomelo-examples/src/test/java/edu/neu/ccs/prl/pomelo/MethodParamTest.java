package edu.neu.ccs.prl.pomelo;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class MethodParamTest {
    @Test
    @Parameters({"77, true", "-9, false"})
    public void test(int param1, boolean param2) {
        if (param1 == 42 && param2) {
            throw new IllegalStateException();
        }
    }

    @Test
    @Parameters({"false"})
    public void test(Object param2) {
        Assume.assumeTrue(param2 != null);
    }

    @Test
    @Parameters({"false"})
    public void test(boolean param2) {
        Assume.assumeFalse(param2);
    }
}