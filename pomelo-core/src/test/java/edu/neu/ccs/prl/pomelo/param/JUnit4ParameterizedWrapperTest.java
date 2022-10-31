package edu.neu.ccs.prl.pomelo.param;

import edu.neu.ccs.prl.pomelo.examples.ParameterizedFieldExample;
import edu.neu.ccs.prl.pomelo.examples.ParameterizedFieldExample;
import edu.neu.ccs.prl.pomelo.fuzz.FixedListFuzzer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.notification.RunNotifier;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class JUnit4ParameterizedWrapperTest {
    @Test
    public void runnerRespectsJunitAnnotations() throws Throwable {
        ParameterizedFieldExample.values.clear();
        new JUnitCore().run(ParameterizedFieldExample.class);
        List<String> expected = new LinkedList<>(ParameterizedFieldExample.values);
        ParameterizedFieldExample.values.clear();
        List<Object[]> parameterGroups = ParameterizedFieldExample.arguments();
        JUnit4ParameterizedWrapper test = new JUnit4ParameterizedWrapper(ParameterizedFieldExample.class, "test1");
        ParameterizedRunner runner = test.createParameterizedRunner(new FixedListFuzzer(parameterGroups));
        runner.run(new RunNotifier());
        Assert.assertEquals(expected, ParameterizedFieldExample.values);
    }

    @Test
    public void parameterGroupsMatchOriginal() throws Throwable {
        List<Object[]> parameterGroups = ParameterizedFieldExample.arguments();
        JUnit4ParameterizedWrapper test = new JUnit4ParameterizedWrapper(ParameterizedFieldExample.class, "test1");
        Assert.assertArrayEquals(parameterGroups.toArray(), test.getOriginalParameterGroups().toArray());
    }
}