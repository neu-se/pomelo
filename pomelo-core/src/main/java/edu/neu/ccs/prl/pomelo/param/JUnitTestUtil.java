package edu.neu.ccs.prl.pomelo.param;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;

public final class JUnitTestUtil {
    private JUnitTestUtil() {
        throw new AssertionError();
    }

    public static FrameworkMethod findFrameworkMethod(Class<? extends Annotation> annotationClass, TestClass testClass,
                                                      String methodName) {
        return testClass.getAnnotatedMethods(annotationClass)
                        .stream()
                        .filter(m -> m.getName().equals(methodName))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Unable to find test method"));
    }
}
