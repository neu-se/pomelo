package edu.neu.ccs.prl.pomelo;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
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
}