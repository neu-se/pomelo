package edu.neu.ccs.prl.pomelo.fuzz;

import edu.neu.ccs.prl.pomelo.fuzz.examples.ParameterizedConstructorExample;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

public class FuzzingTrialRunnerTest {
    @Test
    public void frameworkMethodFound() {
        FrameworkMethod method =
                FuzzingTrialRunner.getFrameworkMethod(new TestClass(ParameterizedConstructorExample.class), "test1");
        Assert.assertNotNull(method);
        Assert.assertEquals("test1", method.getName());
    }
}