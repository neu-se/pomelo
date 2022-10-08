package edu.neu.ccs.prl.pomelo.test;

import edu.neu.ccs.prl.pomelo.examples.JUnitParamsExample;
import edu.neu.ccs.prl.pomelo.examples.ParameterizedFieldExample;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.notification.RunNotifier;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class JUnitParamsWrapperTest {
    @Test
    public void isTypeMatch() {
        Assert.assertTrue(JUnitParamsWrapper.isType(JUnitParamsExample.class));
    }

    @Test
    public void isTypeMismatch() {
        Assert.assertFalse(JUnitParamsWrapper.isType(ParameterizedFieldExample.class));
    }

    @Test
    public void runnerRespectsJunitAnnotations() throws Throwable {
        JUnitParamsExample.values.clear();
        new JUnitCore().run(Request.method(JUnitParamsExample.class, "test1"));
        List<String> expected = new LinkedList<>(JUnitParamsExample.values);
        JUnitParamsExample.values.clear();
        List<Object[]> parameterGroups = Arrays.asList(new Object[]{77, true}, new Object[]{-9, false});
        JUnitParamsWrapper test = new JUnitParamsWrapper(JUnitParamsExample.class, "test1");
        ParameterizedRunner runner = test.createParameterizedRunner(new ListParameterSupplier(parameterGroups));
        runner.run(new RunNotifier());
        Assert.assertEquals(expected, JUnitParamsExample.values);
    }

    @Test
    public void parameterGroupsMatchOriginal() throws Throwable {
        List<Object[]> parameterGroups = Arrays.asList(new Object[]{77, true}, new Object[]{-9, false});
        JUnitParamsWrapper test = new JUnitParamsWrapper(JUnitParamsExample.class, "test1");
        Assert.assertArrayEquals(parameterGroups.toArray(), test.getOriginalParameterGroups().toArray());
    }
}