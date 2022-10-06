package edu.neu.ccs.prl.pomelo.fuzz;

import edu.neu.ccs.prl.pomelo.fuzz.examples.JUnitParamsExample;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

import java.util.LinkedList;
import java.util.List;

public class FuzzingJUnitParamsRunnerTest {
    @Test
    public void junitAnnotationsRespected() throws Throwable {
        JUnitParamsExample.values.clear();
        new JUnitCore().run(Request.method(JUnitParamsExample.class, "test1"));
        List<String> expected = new LinkedList<>(JUnitParamsExample.values);
        Guidance guidance = new TestGuidance(2);
        JUnitParamsExample.values.clear();
        Runner runner = new FuzzingJUnitParamsRunner(JUnitParamsExample.class, "test1", guidance);
        runner.run(new RunNotifier());
        Assert.assertEquals(expected, JUnitParamsExample.values);
    }
}