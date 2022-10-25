package edu.neu.ccs.prl.pomelo.param;

import edu.neu.ccs.prl.pomelo.scan.TestMethod;

public enum ParameterizedTestType {
    JUNIT4_PARAMETERIZED() {
        @Override
        public boolean matches(Class<?> clazz, String methodName) {
            return JUnit4ParameterizedWrapper.isType(clazz);
        }

        @Override
        public ParameterizedTestWrapper wrap(Class<?> clazz, String methodName) {
            return new JUnit4ParameterizedWrapper(clazz, methodName);
        }
    }, JUNIT_PARAMS() {
        @Override
        public boolean matches(Class<?> clazz, String methodName) {
            return JUnitParamsWrapper.isType(clazz);
        }

        @Override
        public ParameterizedTestWrapper wrap(Class<?> clazz, String methodName) {
            return new JUnitParamsWrapper(clazz, methodName);
        }
    };

    public abstract boolean matches(Class<?> clazz, String methodName);

    public abstract ParameterizedTestWrapper wrap(Class<?> clazz, String methodName);

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

    public static ParameterizedTestWrapper findAndWrap(TestMethod method) {
        return findAndWrap(method.getTestClass(), method.getTestMethodName());
    }

    public static ParameterizedTestWrapper findAndWrap(Class<?> testClass, String testMethodName) {
        ParameterizedTestType type = findType(testClass, testMethodName);
        if (type == null) {
            throw new IllegalArgumentException(
                    String.format("%s#%s is not a parameterized test", testClass, testMethodName));
        } else {
            return type.wrap(testClass, testMethodName);
        }
    }
}
