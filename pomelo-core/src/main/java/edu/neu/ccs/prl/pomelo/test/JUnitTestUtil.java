package edu.neu.ccs.prl.pomelo.test;

import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

public final class JUnitTestUtil {
    private JUnitTestUtil() {
        throw new AssertionError();
    }

    public static FrameworkMethod findFrameworkMethod(TestClass testClass, String methodName) {
        return testClass.getAnnotatedMethods(Test.class).stream().filter(m -> m.getName().equals(methodName))
                        .findFirst().orElseThrow(() -> new IllegalArgumentException("Unable to find test method"));
    }
}
