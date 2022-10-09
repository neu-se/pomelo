package edu.neu.ccs.prl.pomelo.param;

import edu.neu.ccs.prl.pomelo.scan.TestMethod;

public enum ParameterizedTestType {
    JUNIT4_PARAMETERIZED() {
        @Override
        public boolean matches(Class<?> clazz) {
            return JUnit4ParameterizedWrapper.isType(clazz);
        }

        @Override
        public ParameterizedTestWrapper wrap(Class<?> clazz, String methodName) {
            return new JUnit4ParameterizedWrapper(clazz, methodName);
        }
    }, JUNIT_PARAMS() {
        @Override
        public boolean matches(Class<?> clazz) {
            return JUnitParamsWrapper.isType(clazz);
        }

        @Override
        public ParameterizedTestWrapper wrap(Class<?> clazz, String methodName) {
            return new JUnitParamsWrapper(clazz, methodName);
        }
    };

    public abstract boolean matches(Class<?> clazz);

    public abstract ParameterizedTestWrapper wrap(Class<?> clazz, String methodName);

    public static boolean isParameterized(TestMethod method) {
        for (ParameterizedTestType type : values()) {
            if (type.matches(method.getTestClass())) {
                return true;
            }
        }
        return false;
    }

    public static ParameterizedTestWrapper wrap(TestMethod method) {
        for (ParameterizedTestType type : values()) {
            if (type.matches(method.getTestClass())) {
                return type.wrap(method.getTestClass(), method.getTestMethodName());
            }
        }
        throw new IllegalArgumentException(method + " is not a parameterized test");
    }
}
