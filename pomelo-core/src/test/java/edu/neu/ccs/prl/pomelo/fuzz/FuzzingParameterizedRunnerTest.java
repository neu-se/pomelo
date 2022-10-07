package edu.neu.ccs.prl.pomelo.fuzz;

import edu.neu.ccs.prl.pomelo.fuzz.examples.ParameterizedFieldExample;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

import java.util.LinkedList;
import java.util.List;

public class FuzzingParameterizedRunnerTest {
    @Test
    public void junitAnnotationsRespected() throws Throwable {
        ParameterizedFieldExample.values.clear();
        new JUnitCore().run(ParameterizedFieldExample.class);
        List<String> expected = new LinkedList<>(ParameterizedFieldExample.values);
        Fuzzer fuzzer = new TestFuzzer(2);
        ParameterizedFieldExample.values.clear();
        Runner runner = new FuzzingParameterizedRunner(ParameterizedFieldExample.class, "test1", fuzzer);
        runner.run(new RunNotifier());
        Assert.assertEquals(expected, ParameterizedFieldExample.values);
    }
}