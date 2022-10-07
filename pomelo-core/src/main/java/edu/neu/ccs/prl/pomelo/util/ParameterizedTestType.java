package edu.neu.ccs.prl.pomelo.util;

import junitparams.JUnitParamsRunner;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

public enum ParameterizedTestType {
    JUNIT4_PARAMETERIZED() {
        @Override
        public boolean matches(Class<?> clazz) {
            return clazz.isAnnotationPresent(RunWith.class) &&
                    clazz.getAnnotation(RunWith.class).value().equals(Parameterized.class);
        }
    }, JUNIT_PARAMS() {
        @Override
        public boolean matches(Class<?> clazz) {
            return clazz.isAnnotationPresent(RunWith.class) &&
                    clazz.getAnnotation(RunWith.class).value().equals(JUnitParamsRunner.class);
        }
    };

    public abstract boolean matches(Class<?> clazz);

    public static boolean isParameterizedTest(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        for (ParameterizedTestType type : values()) {
            if (type.matches(clazz)) {
                return true;
            }
        }
        return false;
    }

    public static ParameterizedTestType findType(Class<?> clazz) {
        for (ParameterizedTestType type : values()) {
            if (type.matches(clazz)) {
                return type;
            }
        }
        throw new IllegalArgumentException(clazz + " is not a parameterized test");
    }
}
