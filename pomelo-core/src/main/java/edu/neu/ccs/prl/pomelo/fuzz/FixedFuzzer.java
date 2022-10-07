package edu.neu.ccs.prl.pomelo.fuzz;

import edu.neu.ccs.prl.pomelo.util.ParameterizedTestType;
import junitparams.internal.TestMethod;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FixedFuzzer implements Fuzzer {
    private final LinkedList<Object[]> items;

    public FixedFuzzer(List<Object[]> items) {
        this.items = new LinkedList<>(items);
    }

    @Override
    public void setUp(Class<?> testClass, String testMethodName) {
    }

    @Override
    public void tearDown() {
    }

    @Override
    public boolean hasNext() {
        return !items.isEmpty();
    }

    @Override
    public Object[] next() {
        return items.poll();
    }

    @Override
    public void handleResult(Object[] arguments, Throwable failure) {
    }

    public static FixedFuzzer withOriginalArguments(Class<?> clazz, String methodName) {
        TestClass testClass = new TestClass(clazz);
        FrameworkMethod testMethod = FuzzingTrialRunner.getFrameworkMethod(testClass, methodName);
        switch (ParameterizedTestType.findType(clazz)) {
            case JUNIT_PARAMS:
                return new FixedFuzzer(getJUnitParamsOriginalItems(testClass, testMethod));
            case JUNIT4_PARAMETERIZED:
                // TODO
            default:
                throw new AssertionError();
        }
    }

    private static List<Object[]> getJUnitParamsOriginalItems(TestClass testClass, FrameworkMethod testMethod) {
        Object[] parametersSets = new TestMethod(testMethod, testClass).parametersSets();
        RecordingFrameworkMethod m = new RecordingFrameworkMethod(testMethod.getMethod());
        for (int i = 0; i < parametersSets.length; i++) {
            try {
                createInvokeParameterisedMethod(testClass, m, parametersSets[i], i).evaluate();
            } catch (Throwable e) {
                throw new IllegalStateException("Failed to collect parameter for " + testMethod, e);
            }
        }
        return m.items;
    }

    private static Statement createInvokeParameterisedMethod(TestClass testClass, FrameworkMethod testMethod,
                                                             Object parametersSet, int index)
            throws ReflectiveOperationException {
        Constructor<?> c = Class.forName("junitparams.internal.InvokeParameterisedMethod")
                                .getDeclaredConstructor(FrameworkMethod.class, Object.class, Object.class, int.class);
        c.setAccessible(true);
        return (Statement) c.newInstance(testMethod, testClass, parametersSet, index);
    }

    private static final class RecordingFrameworkMethod extends FrameworkMethod {
        List<Object[]> items = new ArrayList<>();

        public RecordingFrameworkMethod(Method method) {
            super(method);
        }

        @Override
        public Object invokeExplosively(Object target, Object... params) {
            items.add(params);
            return null;
        }
    }
}
