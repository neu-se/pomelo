package edu.neu.ccs.prl.pomelo.test;

import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParameters;
import org.junit.runners.parameterized.TestWithParameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class JUnit4ParameterizedWrapper implements ParameterizedTestWrapper {
    private final Class<?> testClass;
    private final String testMethodName;

    public JUnit4ParameterizedWrapper(Class<?> testClass, String testMethodName) {
        if (testClass == null || testMethodName == null) {
            throw new NullPointerException();
        } else if (!isType(testClass)) {
            throw new IllegalArgumentException(testClass + " is not a JUnitParams test");
        }
        this.testClass = testClass;
        this.testMethodName = testMethodName;
    }

    @Override
    public ParameterizedRunner createParameterizedRunner(ParameterSupplier supplier) throws Throwable {
        return new Runner(testClass, testMethodName, supplier);
    }

    @Override
    public List<Object[]> getOriginalParameterGroups() throws Throwable {
        Object parameters = getParametersMethod(new TestClass(testClass)).invokeExplosively(null);
        Iterable<?> allParameters;
        if (parameters instanceof Iterable) {
            allParameters = (Iterable<?>) parameters;
        } else if (parameters instanceof Object[]) {
            allParameters = Arrays.asList((Object[]) parameters);
        } else {
            throw new IllegalStateException("Invalid Parameters method");
        }
        List<Object[]> result = new ArrayList<>();
        for (Object p : allParameters) {
            result.add((p instanceof Object[]) ? (Object[]) p : new Object[]{p});
        }
        return result;
    }

    public static boolean isType(Class<?> clazz) {
        return clazz.isAnnotationPresent(RunWith.class) &&
                clazz.getAnnotation(RunWith.class).value().equals(Parameterized.class);
    }

    private static FrameworkMethod getParametersMethod(TestClass testClass) {
        return testClass.getAnnotatedMethods(Parameterized.Parameters.class).stream().filter(FrameworkMethod::isPublic)
                        .filter(FrameworkMethod::isStatic).findFirst().orElseThrow(IllegalStateException::new);
    }

    private static class Runner extends Parameterized implements ParameterizedRunner {
        private final ParameterSupplier supplier;
        private final FrameworkMethod method;

        private Runner(Class<?> clazz, String methodName, ParameterSupplier supplier) throws Throwable {
            super(clazz);
            if (supplier == null) {
                throw new NullPointerException();
            }
            this.supplier = supplier;
            this.method = JUnitTestUtil.findFrameworkMethod(getTestClass(), methodName);
        }

        @Override
        protected Statement childrenInvoker(RunNotifier notifier) {
            return createStatement(notifier, supplier);
        }

        @Override
        public void runWithParameterGroup(RunNotifier notifier, Object[] parameterGroup) throws InitializationError {
            new BlockJUnit4ClassRunnerWithParameters(
                    new TestWithParameters("[0]", getTestClass(), Arrays.asList(parameterGroup))) {
                @Override
                protected void runChild(FrameworkMethod method, RunNotifier notifier) {
                    if (method.equals(Runner.this.method)) {
                        super.runChild(method, notifier);
                    }
                }
            }.run(notifier);
        }
    }
}
