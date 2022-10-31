package edu.neu.ccs.prl.pomelo.param;

import edu.neu.ccs.prl.pomelo.examples.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

import static edu.neu.ccs.prl.pomelo.param.ParameterizedTestType.*;

public class ParameterizedTestTypeTest {
    @ParameterizedTest
    @MethodSource("arguments")
    public void testFindCorrectType(ParameterizedTestType expected, Class<?> testClass, String testMethodName) {
        Assertions.assertEquals(expected, ParameterizedTestType.findType(testClass, testMethodName));
    }

    @SuppressWarnings("unused")
    public static List<Arguments> arguments() {
        return Arrays.asList(Arguments.of(JUNIT_PARAMS, JUnitParamsExample.class, "test1"),
                             Arguments.of(null, MyStringGenerator.class, "generate"),
                             Arguments.of(JUNIT4_PARAMETERIZED, ParameterizedConstructorExample.class, "test1"),
                             Arguments.of(JUNIT4_PARAMETERIZED, ParameterizedFieldExample.class, "test1"),
                             Arguments.of(JUNIT4_PARAMETERIZED, ParameterizedFieldFromExample.class, "test1"),
                             Arguments.of(JQF_FUZZ, JqfExample.class, "test1"));
    }
}