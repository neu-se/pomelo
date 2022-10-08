package edu.neu.ccs.prl.pomelo.test;

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

    public static boolean isParameterizedTest(Class<?> clazz) {
        for (ParameterizedTestType type : values()) {
            if (type.matches(clazz)) {
                return true;
            }
        }
        return false;
    }

    public static ParameterizedTestType getType(Class<?> clazz) {
        for (ParameterizedTestType type : values()) {
            if (type.matches(clazz)) {
                return type;
            }
        }
        throw new IllegalArgumentException(clazz + " is not a parameterized test");
    }
}
