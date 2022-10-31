package edu.neu.ccs.prl.pomelo.param;

import edu.neu.ccs.prl.pomelo.scan.TestMethod;
import org.junit.runner.RunWith;

public enum ParameterizedTestType {
    JUNIT4_PARAMETERIZED() {
        @Override
        public boolean matches(Class<?> clazz, String methodName) {
            return runsWithClass("org.junit.runners.Parameterized", clazz);
        }

        @Override
        public ParameterizedTest wrap(Class<?> clazz, String methodName) {
            return new JUnit4ParameterizedWrapper(clazz, methodName);
        }
    }, JUNIT_PARAMS() {
        @Override
        public boolean matches(Class<?> clazz, String methodName) {
            return runsWithClass("junitparams.JUnitParamsRunner", clazz);
        }

        @Override
        public ParameterizedTest wrap(Class<?> clazz, String methodName) {
            return new JUnitParamsWrapper(clazz, methodName);
        }
    }, JQF_FUZZ() {
        @Override
        public boolean matches(Class<?> clazz, String methodName) {
            return runsWithClass("edu.berkeley.cs.jqf.fuzz.JQF", clazz);
        }

        @Override
        public ParameterizedTest wrap(Class<?> clazz, String methodName) {
            return new JqfWrapper(clazz, methodName);
        }
    };

    public abstract boolean matches(Class<?> clazz, String methodName);

    public abstract ParameterizedTest wrap(Class<?> clazz, String methodName);

    public static ParameterizedTestType findType(Class<?> clazz, String methodName) {
        for (ParameterizedTestType type : values()) {
            if (type.matches(clazz, methodName)) {
                return type;
            }
        }
        return null;
    }

    public static boolean isParameterized(TestMethod method) {
        return findType(method.getTestClass(), method.getTestMethodName()) != null;
    }

    public static ParameterizedTest findAndWrap(TestMethod method) {
        return findAndWrap(method.getTestClass(), method.getTestMethodName());
    }

    public static ParameterizedTest findAndWrap(Class<?> testClass, String testMethodName) {
        ParameterizedTestType type = findType(testClass, testMethodName);
        if (type == null) {
            throw new IllegalArgumentException(
                    String.format("%s#%s is not a parameterized test", testClass, testMethodName));
        } else {
            return type.wrap(testClass, testMethodName);
        }
    }

    private static boolean runsWithClass(String name, Class<?> clazz) {
        return clazz.isAnnotationPresent(RunWith.class) &&
                clazz.getAnnotation(RunWith.class).value().getName().equals(name);
    }
}