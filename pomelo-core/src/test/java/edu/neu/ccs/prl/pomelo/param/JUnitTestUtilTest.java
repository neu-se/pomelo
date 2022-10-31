package edu.neu.ccs.prl.pomelo.param;

import edu.neu.ccs.prl.pomelo.examples.ParameterizedConstructorExample;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

public class JUnitTestUtilTest {

    @Test
    public void findsCorrectFrameworkMethod() {
        FrameworkMethod method =
                JUnitTestUtil.findFrameworkMethod(Test.class, new TestClass(ParameterizedConstructorExample.class),
                                                  "test1");
        Assert.assertNotNull(method);
        Assert.assertEquals("test1", method.getName());
    }
}