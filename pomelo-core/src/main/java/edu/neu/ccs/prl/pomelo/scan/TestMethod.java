package edu.neu.ccs.prl.pomelo.scan;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class TestMethod {
    private final Class<?> testClass;
    private final String testMethodName;

    public TestMethod(Class<?> testClass, String testMethodName) {
        if (testClass == null || testMethodName == null) {
            throw new NullPointerException();
        }
        this.testClass = testClass;
        this.testMethodName = testMethodName;
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public String getTestMethodName() {
        return testMethodName;
    }

    @Override
    public int hashCode() {
        int result = testClass.hashCode();
        result = 31 * result + testMethodName.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof TestMethod)) {
            return false;
        }
        TestMethod that = (TestMethod) o;
        if (!testClass.equals(that.testClass)) {
            return false;
        }
        return testMethodName.equals(that.testMethodName);
    }

    @Override
    public String toString() {
        return testClass + "#" + testMethodName;
    }

    public List<Method> findMethod(Class<? extends Annotation> annotationClass) {
        return Arrays.stream(testClass.getMethods())
                     .filter(m -> m.isAnnotationPresent(annotationClass))
                     .filter(m -> m.getName().equals(testMethodName))
                     .collect(Collectors.toList());
    }

    public static String getMethodName(String displayName) {
        int i;
        if ((i = displayName.indexOf('(')) != -1) {
            displayName = displayName.substring(0, i);
        }
        if ((i = displayName.indexOf('[')) != -1) {
            displayName = displayName.substring(0, i);
        }
        return displayName;
    }
}
