package edu.neu.ccs.prl.pomelo.examples;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class ConfigTest {
    @Test
    @Parameters({"true"})
    public void test(boolean param) {
        // Check property set via argument line
        Assert.assertEquals("hello", System.getProperty("pomelo.examples.value0"));
        // Check environment variable
        Assert.assertEquals("world", System.getenv("pomelo_examples_value1"));
        // Check system property
        Assert.assertEquals("!", System.getProperty("pomelo.examples.value2"));
    }
}
